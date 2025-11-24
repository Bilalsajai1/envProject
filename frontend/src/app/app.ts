import {Component, inject, OnInit, signal} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {environment} from '../configuration/environement.config';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.scss'
})
export class App implements OnInit {
  isSidebarCollapsed: boolean = true;
  private translate = inject(TranslateService);

  ngOnInit(): void {
    this.initializeTranslation();

  }
  private initializeTranslation(): void {
    const supportedLangs = environment.languages;
    const storedLang = localStorage.getItem('user-language');


    // Determine which language to use
    const languageToUse = storedLang && supportedLangs.includes(storedLang)
      ? storedLang
      : 'fr';


    // Ensure the language is set in localStorage
    if (!storedLang || !supportedLangs.includes(storedLang)) {
      localStorage.setItem('user-language', languageToUse);
    }

    this.translate.use(languageToUse);
  }
}

