import { useQuery } from '@tanstack/vue-query'
import { api } from '@/lib/axios'
import { useCampaignStore } from '@/stores/campaign'
import type { SelectionCampaign } from '@/types'

export function useCurrentCampaign() {
  const store = useCampaignStore()

  return useQuery({
    queryKey: ['campaign', 'current'],
    queryFn: async () => {
      try {
        const { data } = await api.get('/campaigns/current')
        const campaign = data.data as SelectionCampaign
        store.setCampaign(campaign)
        if (campaign.status === 'ACTIVE') {
          store.startCountdown()
        }
        return campaign
      } catch {
        store.setCampaign(null)
        return null
      }
    },
    refetchInterval: 30_000,
    retry: false,
  })
}