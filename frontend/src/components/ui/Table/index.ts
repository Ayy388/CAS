import { type Component } from 'vue'
import TableComponent from './Table.vue'
import TableBodyComponent from '../TableBody/TableBody.vue'
import TableCellComponent from '../TableCell/TableCell.vue'
import TableHeadComponent from '../TableHead/TableHead.vue'
import TableHeaderComponent from '../TableHeader/TableHeader.vue'
import TableRowComponent from '../TableRow/TableRow.vue'

export const Table = TableComponent as Component
export const TableBody = TableBodyComponent as Component
export const TableCell = TableCellComponent as Component
export const TableHead = TableHeadComponent as Component
export const TableHeader = TableHeaderComponent as Component
export const TableRow = TableRowComponent as Component