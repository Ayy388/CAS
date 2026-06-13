$baseUrl = "http://localhost:8080/api/v1"
$timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()

function Write-Step($msg) {
    Write-Host "`n==============================" -ForegroundColor Cyan
    Write-Host "  $msg" -ForegroundColor Yellow
    Write-Host "==============================" -ForegroundColor Cyan
}

function Call-Api($method, $url, $token, $body) {
    $headers = @{}
    if ($token) { $headers["Authorization"] = "Bearer $token" }
    if ($body) { $headers["Content-Type"] = "application/json" }

    $params = @{
        Method = $method
        Uri = $url
        Headers = $headers
    }
    if ($body) { $params["Body"] = ($body | ConvertTo-Json -Depth 10) }

    try {
        $resp = Invoke-RestMethod @params
        return $resp
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        try {
            $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
            $errBody = $reader.ReadToEnd() | ConvertFrom-Json
            Write-Host "  [${statusCode}] $($errBody.message)" -ForegroundColor Red
            return $errBody
        } catch {
            Write-Host "  [${statusCode}] $($_.Exception.Message)" -ForegroundColor Red
            return $null
        }
    }
}

# ==============================
# 1. Admin Login
# ==============================
Write-Step "1. Admin Login"
$adminLogin = Call-Api POST "$baseUrl/auth/login" $null @{username="admin"; password="admin123"}
$adminToken = $adminLogin.data.token
Write-Host "  Token: $($adminToken.Substring(0,20))..." -ForegroundColor Green

# ==============================
# 2. Create Semester
# ==============================
Write-Step "2. Create Semester"
$semester = Call-Api POST "$baseUrl/admin/semesters" $adminToken @{
    name = "E2E测试学期-$timestamp"
    academicYear = "2025-2026"
    semesterType = "FIRST"
    startDate = "2025-09-01"
    endDate = "2026-01-15"
}
$semesterId = $semester.data.id
Write-Host "  Semester ID: $semesterId" -ForegroundColor Green

# ==============================
# 3. Activate Semester
# ==============================
Write-Step "3. Activate Semester"
$activateResp = Call-Api PATCH "$baseUrl/admin/semesters/$semesterId/activate" $adminToken
if ($activateResp.code -eq 200) {
    Write-Host "  Activated" -ForegroundColor Green
} else {
    Write-Host "  Activate result: $($activateResp.message)" -ForegroundColor Yellow
}

# ==============================
# 4. Create Teacher User (via admin, or direct - use ensure from tests)
# ==============================
Write-Step "4. Create Teacher & Student (seed if needed)"
# We'll use existing seed users from tests or create via registration
# First check if e2e users exist by trying to login
$teacherLogin = Call-Api POST "$baseUrl/auth/login" $null @{username="tch_e2e"; password="123456"}
if (-not $teacherLogin.data) {
    Write-Host "  Creating teacher..." -ForegroundColor Yellow
    # Can't create users via API, need to use the test approach or check if there's a user endpoint
    # For now, we'll use existing test users or fallback
    $teacherLogin = Call-Api POST "$baseUrl/auth/login" $null @{username="tch_rem"; password="123456"}
}
$teacherToken = $teacherLogin.data.token
Write-Host "  Teacher: $($teacherLogin.data.realName)" -ForegroundColor Green

$studentLogin = Call-Api POST "$baseUrl/auth/login" $null @{username="stu_e2e"; password="123456"}
if (-not $studentLogin.data) {
    Write-Host "  Trying stu_rem..." -ForegroundColor Yellow
    $studentLogin = Call-Api POST "$baseUrl/auth/login" $null @{username="stu_rem"; password="123456"}
    if (-not $studentLogin.data) {
        Write-Host "  Trying stu_enroll..." -ForegroundColor Yellow
        $studentLogin = Call-Api POST "$baseUrl/auth/login" $null @{username="stu_enroll"; password="123456"}
    }
}
$studentToken = $studentLogin.data.token
$studentId = $studentLogin.data.id
$studentRealName = $studentLogin.data.realName
Write-Host "  Student: $studentRealName (ID: $studentId)" -ForegroundColor Green

# Get admin's /me to get admin id
$adminMe = Call-Api GET "$baseUrl/auth/me" $adminToken
$adminId = $adminMe.data.id
Write-Host "  Admin ID: $adminId" -ForegroundColor Green

# ==============================
# 5. Create Course
# ==============================
Write-Step "5. Create Course (ELECTIVE type)"
$course = Call-Api POST "$baseUrl/admin/courses" $adminToken @{
    code = "E2E$timestamp"
    name = "端到端测试课程"
    type = "ELECTIVE_GENERAL"
    credits = 2.0
    hours = 32
    description = "E2E测试用课程"
}
$courseId = $course.data.id
Write-Host "  Course ID: $courseId - $($course.data.name)" -ForegroundColor Green

# ==============================
# 6. Create Offering
# ==============================
Write-Step "6. Create Offering"
# Get teacher ID from /me
$tchMe = Call-Api GET "$baseUrl/auth/me" $teacherToken
$teacherUserId = $tchMe.data.id
Write-Host "  Teacher User ID: $teacherUserId" -ForegroundColor Green

$offering = Call-Api POST "$baseUrl/admin/offerings" $adminToken @{
    semesterId = $semesterId
    courseId = $courseId
    teacherId = $teacherUserId
    maxCapacity = 30
    minEnrollment = 5
    openGrade = "2024"
}
$offeringId = $offering.data.id
Write-Host "  Offering ID: $offeringId" -ForegroundColor Green

# ==============================
# 7. Create Campaign
# ==============================
Write-Step "7. Create Campaign"
# Campaign time must cover "now"
$startTime = [DateTime]::Now.AddMinutes(-1).ToString("yyyy-MM-ddTHH:mm:ss")
$endTime = [DateTime]::Now.AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss")
$campaign = Call-Api POST "$baseUrl/admin/campaigns" $adminToken @{
    name = "E2E测试活动-$timestamp"
    semesterId = $semesterId
    startTime = $startTime
    endTime = $endTime
}
$campaignId = $campaign.data.id
Write-Host "  Campaign ID: $campaignId - status: $($campaign.data.status)" -ForegroundColor Green

# ==============================
# 8. Start Campaign
# ==============================
Write-Step "8. Start Campaign"
$startCamp = Call-Api PATCH "$baseUrl/admin/campaigns/$campaignId/start" $adminToken
Write-Host "  Status: $($startCamp.data.status)" -ForegroundColor Green

# ==============================
# 9. Student Enroll
# ==============================
Write-Step "9. Student Enroll"
Start-Sleep -Seconds 1
$enroll = Call-Api POST "$baseUrl/student/enroll" $studentToken @{ offeringId = $offeringId }
if ($enroll.code -eq 200) {
    $enrollmentId = $enroll.data.id
    Write-Host "  ✅ Enrollment ID: $enrollmentId" -ForegroundColor Green
} else {
    Write-Host "  ❌ $($enroll.message)" -ForegroundColor Red
    $enrollmentId = $null
}

# ==============================
# 10. Student View My Enrollments
# ==============================
Write-Step "10. Student My Enrollments"
$myEnroll = Call-Api GET "$baseUrl/student/enrollments" $studentToken
Write-Host "  Enrollments count: $(($myEnroll.data | Measure-Object).Count)" -ForegroundColor Green
$myEnroll.data | ForEach-Object { Write-Host "    - $($_.courseName) ($($_.status))" }

# ==============================
# 11. Student Drop & Re-enroll
# ==============================
Write-Step "11. Student Drop Enrollment"
if ($enrollmentId) {
    $dropResp = Call-Api DELETE "$baseUrl/student/enrollments/$enrollmentId" $studentToken
    if ($dropResp.code -eq 200) {
        Write-Host "  ✅ Dropped successfully" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $($dropResp.message)" -ForegroundColor Red
    }

    # Re-enroll
    Write-Step "12. Student Re-enroll"
    Start-Sleep -Seconds 3 # wait for Redis dedup TTL to expire
    $reEnroll = Call-Api POST "$baseUrl/student/enroll" $studentToken @{ offeringId = $offeringId }
    if ($reEnroll.code -eq 200) {
        $enrollmentId = $reEnroll.data.id
        Write-Host "  ✅ Re-enrolled ID: $enrollmentId" -ForegroundColor Green
    } else {
        Write-Host "  ❌ $($reEnroll.message)" -ForegroundColor Red
    }
}

# ==============================
# 13. Admin End Campaign
# ==============================
Write-Step "13. End Campaign"
$endCamp = Call-Api PATCH "$baseUrl/admin/campaigns/$campaignId/end" $adminToken
Write-Host "  Status: $($endCamp.data.status)" -ForegroundColor Green

# ==============================
# 14. Admin Review (Approve)
# ==============================
Write-Step "14. Admin Approve Offering"
$reviewResp = Call-Api POST "$baseUrl/admin/review" $adminToken @{
    offeringId = $offeringId
    action = "APPROVED"
    remark = "审核通过"
}
if ($reviewResp.code -eq 200) {
    Write-Host "  ✅ Approved" -ForegroundColor Green
} else {
    Write-Host "  ❌ $($reviewResp.message)" -ForegroundColor Red
}

# ==============================
# 15. Student Check Notifications
# ==============================
Write-Step "15. Student Check Notifications"
Start-Sleep -Seconds 2
$notifs = Call-Api GET "$baseUrl/notifications" $studentToken
$notifCount = $(($notifs.data | Measure-Object).Count)
Write-Host "  Notifications: $notifCount" -ForegroundColor Green
$notifs.data | ForEach-Object { Write-Host "    - $($_.title) | read=$($_.read)" }

# Mark as read
if ($notifCount -gt 0) {
    $firstNotifId = $notifs.data[0].id
    $readResp = Call-Api PUT "$baseUrl/notifications/$firstNotifId/read" $studentToken
    Write-Host "  Marked notification $firstNotifId as read: $($readResp.code)" -ForegroundColor Green
}

# ==============================
# 16. Teacher View
# ==============================
Write-Step "16. Teacher View Courses"
$tchCourses = Call-Api GET "$baseUrl/teacher/courses" $teacherToken
$tchCourses.data | ForEach-Object { Write-Host "    - $($_.courseName) | enrolled=$($_.enrolledCount)/$($_.maxCapacity)" }

Write-Step "17. Teacher View Students"
if ($tchCourses.data.Count -gt 0) {
    $tchOfferingId = $tchCourses.data[0].id
    $students = Call-Api GET "$baseUrl/teacher/courses/$tchOfferingId/students" $teacherToken
    Write-Host "  Students: $(($students.data | Measure-Object).Count)" -ForegroundColor Green
    $students.data | ForEach-Object { Write-Host "    - $($_.studentName) ($($_.studentNo))" }
}

# ==============================
# 18. Dashboard
# ==============================
Write-Step "18. Dashboard Stats"
$stats = Call-Api GET "$baseUrl/admin/dashboard/stats" $adminToken
Write-Host "  totalCourses=$($stats.data.totalCourses) teachers=$($stats.data.totalTeachers) students=$($stats.data.totalStudents) enrollments=$($stats.data.totalEnrollments)" -ForegroundColor Green

$topCourses = Call-Api GET "$baseUrl/admin/dashboard/top-courses" $adminToken
Write-Host "  Top courses: $(($topCourses.data | Measure-Object).Count)" -ForegroundColor Green

$trend = Call-Api GET "$baseUrl/admin/dashboard/trend" $adminToken
Write-Host "  Trend data: $(($trend.data | Measure-Object).Count) entries" -ForegroundColor Green

Write-Host "`n==============================" -ForegroundColor Cyan
Write-Host "  E2E TEST COMPLETE" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Cyan