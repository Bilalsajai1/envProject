export interface MenuAdmin {
  id?: number;
  code: string;
  libelle?: string;
  route?: string;
  icon?: string;
  ordre?: number;
  visible?: boolean;


  parentId?: number | null;
  environmentTypeId?: number | null;
}
