// src/app/shared/components/confirm-dialog/confirm-dialog.component.ts

import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'confirm' | 'warning' | 'danger' | 'info';
  icon?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.scss'],
  standalone: false
})
export class ConfirmDialogComponent {

  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {

    this.data.type = this.data.type || 'confirm';
    this.data.confirmText = this.data.confirmText || 'Confirmer';
    this.data.cancelText = this.data.cancelText || 'Annuler';


    if (!this.data.icon) {
      this.data.icon = this.getDefaultIcon();
    }
  }

  private getDefaultIcon(): string {
    switch (this.data.type) {
      case 'warning':
        return 'warning';
      case 'danger':
        return 'error';
      case 'info':
        return 'info';
      default:
        return 'help_outline';
    }
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }


  get dialogTypeClass(): string {
    return `dialog-${this.data.type}`;
  }
}
