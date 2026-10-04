import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  Output
} from '@angular/core';
import { CommonModule } from '@angular/common';

interface ScanPreview {
  file: File;
  url: string;
}

@Component({
  selector: 'app-scan-uploader',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './scan-uploader.html',
  styleUrl: './scan-uploader.scss'
})
export class ScanUploader implements OnChanges, OnDestroy {

  @Input() scans: File[] = [];

  @Output() scansChange = new EventEmitter<File[]>();

  errorMessage = '';
  previews: ScanPreview[] = [];

  ngOnChanges(): void {
    this.syncPreviews();
  }

  ngOnDestroy(): void {
    this.revokePreviews();
  }

  onFilesSelected(event: Event): void {
    this.errorMessage = '';

    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);

    const validFiles: File[] = [];

    for (const file of files) {
      if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
        this.errorMessage = 'Only JPEG, PNG and WEBP images are allowed.';
        continue;
      }

      if (!file.size || file.size > 5 * 1024 * 1024) {
        this.errorMessage = 'Each scan must be a non-empty image up to 5 MB.';
        continue;
      }

      validFiles.push(file);
    }

    this.scans = [
      ...this.scans,
      ...validFiles
    ];
    this.syncPreviews();
    this.scansChange.emit(this.scans);
    input.value = '';
  }

  remove(index: number): void {
    this.scans = this.scans.filter((_, itemIndex) => itemIndex !== index);
    this.syncPreviews();
    this.scansChange.emit(this.scans);
  }

  private syncPreviews(): void {
    this.revokePreviews();
    this.previews = this.scans.map(file => ({
      file,
      url: URL.createObjectURL(file)
    }));
  }

  private revokePreviews(): void {
    for (const preview of this.previews) {
      URL.revokeObjectURL(preview.url);
    }
    this.previews = [];
  }
}
