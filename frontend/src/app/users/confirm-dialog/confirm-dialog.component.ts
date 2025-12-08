// src/app/shared/components/confirm-dialog/confirm-dialog.component.ts

import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: 'confirm' | 'warning' | 'danger' | 'info'; // Type de dialogue
  icon?: string; // Icône Material optionnelle
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
    // Définir les valeurs par défaut
    this.data.type = this.data.type || 'confirm';
    this.data.confirmText = this.data.confirmText || 'Confirmer';
    this.data.cancelText = this.data.cancelText || 'Annuler';

    // Définir l'icône par défaut selon le type
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

  // Getter pour la classe CSS du type
  get dialogTypeClass(): string {
    return `dialog-${this.data.type}`;
  }
}
