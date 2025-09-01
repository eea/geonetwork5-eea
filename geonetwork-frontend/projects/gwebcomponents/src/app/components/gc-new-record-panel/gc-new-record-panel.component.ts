import {
  Component,
  input,
  Input,
  signal,
  ViewEncapsulation,
} from '@angular/core';
import { GcBaseComponent } from '../gc-base-component';
import { NewRecordPanelComponent } from 'glib';

@Component({
  selector: 'gc-new-record-panel',
  templateUrl: './gc-new-record-panel.component.html',
  imports: [NewRecordPanelComponent],
  styleUrl: './gc-new-record-panel.component.css',
  // encapsulation: ViewEncapsulation.ShadowDom,
})
export class GcNewRecordPanelComponent extends GcBaseComponent {
  searchFilter = input<string | undefined>(undefined, {
    alias: 'search-filter',
  });
  groupOwner = input<string | undefined>(undefined, { alias: 'group-owner' });
  listOfIncludedAnalysisProperties: input<string>('', { alias: 'include-properties' });
  listOfExcludedAnalysisProperties: input<string>('', { alias: 'exclude-properties' });

  includeProperties = signal<string[]>([])
  excludeProperties = signal<string[]>([])

  override ngOnInit() {
    super.ngOnInit();
    this.includeProperties && this.includeProperties.set(this.listOfIncludedAnalysisProperties.split(','));
    this.excludeProperties && this.excludeProperties.set(this.listOfExcludedAnalysisProperties.split(','));
  }
}
