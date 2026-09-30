# AdNir Mobile 1.0 (Android)

यह Android app अपने अंदर browser खोलता है और केवल खुले पेज की दिख रही जानकारी स्कैन करता है। ChatGPT, अपने server या API key की जरूरत नहीं। इंटरनेट और जानकारी दिखाने वाली वेबसाइट की जरूरत है। Google Search का HTML बदलने पर Auto scan बंद हो सकता है; नंबर/पता न दिखने पर खाली रहेंगे। यह Google Places API का विकल्प नहीं है। साइट की शर्तों का पालन करें।

## इस्तेमाल
1. ऐप खोलें, खोज लिखें या https:// लिंक दें और खोलें दबाएँ।
2. पूरी business listings दिखाई दें तो Scan दबाएँ। Preview जाँचें।
3. जोड़ें दबाकर रिकॉर्ड सेव करें; CSV से फोन में डाउनलोड करें।
4. अन्य वेबसाइट पर नीचे CSS selectors भरें।

## GitHub से APK बनाएँ
1. नई GitHub repository में इस folder के **अंदर की सभी files** upload करें; `.github` folder भी रखें।
2. Repository के Actions tab में `Build AdNir Android APK` चुनें और Run workflow दबाएँ।
3. Build सफल हो तो run के नीचे `AdNir-Mobile-debug-APK` artifact डाउनलोड करें। ZIP से `app-debug.apk` निकालकर Android फोन पर install करें।
4. यह debug APK निजी testing के लिए है। सार्वजनिक release के लिए अपने signing key से release APK/AAB बनाना होगा।

इस workspace में Android SDK/Gradle उपलब्ध नहीं है, इसलिए यहाँ APK compile या phone पर test नहीं हुआ।
