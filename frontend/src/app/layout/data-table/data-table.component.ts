import {
  Component,
  Input,
  OnInit,
  ViewChild,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges
} from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { TableConfig } from '../../../configuration/data-table.config';

@Component({
  selector: 'app-data-table',
  standalone: false,
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss']
})
export class DataTableComponent implements OnInit, OnChanges {

  @Input() data: any[] = [];
  @Input() config!: TableConfig;

  @Output() edit = new EventEmitter<any>();
  @Output() delete = new EventEmitter<any>();

  displayedColumns: string[] = [];
  dataSource: MatTableDataSource<any> = new MatTableDataSource<any>([]);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  ngOnInit() {
    this.displayedColumns = [
      ...this.config.columns.map(c => c.field),
      'actions'
    ];
    this.dataSource = new MatTableDataSource(this.data);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['data']) {
      this.dataSource = new MatTableDataSource(this.data);
      if (this.config?.enablePagination && this.paginator) {
        this.dataSource.paginator = this.paginator;
      }
      if (this.config?.enableSort && this.sort) {
        this.dataSource.sort = this.sort;
      }
    }
  }

  ngAfterViewInit() {
    if (this.config.enablePagination) {
      this.dataSource.paginator = this.paginator;
    }
    if (this.config.enableSort) {
      this.dataSource.sort = this.sort;
    }
  }

  applyFilter(event: Event) {
    if (!this.config.enableSearch) return;
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  onEdit(row: any) {
    this.edit.emit(row);
  }

  onDelete(row: any) {
    this.delete.emit(row);
  }
}
