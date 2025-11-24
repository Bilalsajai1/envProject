import {Component, inject, OnInit, signal} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {environment} from '../configuration/environement.config';
import {AuthService} from './auth/auth-service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.scss'
})
export class App implements OnInit {
  constructor(private auth: AuthService) {}

  private translate = inject(TranslateService);

  ngOnInit(): void {
    this.auth.init().subscribe({
      next: () => console.log("Auth loaded"),
      error: () => console.warn("Not authenticated yet")
    });
    this.initializeTranslation();
  }
  private initializeTranslation(): void {
    const supportedLangs = environment.languages;
    const storedLang = localStorage.getItem('user-language');


    const languageToUse = storedLang && supportedLangs.includes(storedLang)
      ? storedLang
      : 'fr';

    if (!storedLang || !supportedLangs.includes(storedLang)) {
      localStorage.setItem('user-language', languageToUse);
    }

    this.translate.use(languageToUse);
  }
}

