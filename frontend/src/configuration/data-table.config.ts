export interface ColumnDef {
  field: string;
  header: string;
  type?: 'text' | 'boolean' | 'date' | 'number';
  width?: string;
}

export interface TableConfig {
  columns: ColumnDef[];
  enableSearch?: boolean;
  enableFilter?: boolean;
  enableSort?: boolean;
  enablePagination?: boolean;
}
