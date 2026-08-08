package com.example.kinetic

/**
 * Translates the Romanian-source tips (TipsBank) into the app's supported languages.
 * Romanian is the source language of the tip bank, so "ro" returns the text as-is.
 */
object TipsTranslator {
    fun translateTip(tipText: String, lang: String): String {
        val language = lang.lowercase()
        if (language == "ro" || language.startsWith("ro")) return tipText
        // Normalize regional variants (e.g. "en-US", "pt-BR") to the base code.
        val baseLang = language.substringBefore("-")
        val translationsMap = when (baseLang) {
            "en" -> enTranslations
            "ru" -> ruTranslations
            "uk" -> ukTranslations
            "fr" -> frTranslations
            "de" -> deTranslations
            "es" -> esTranslations
            "it" -> itTranslations
            "tr" -> trTranslations
            "pt" -> ptTranslations
            "pl" -> plTranslations
            else -> emptyMap()
        }
        return translationsMap[tipText] ?: tipText
    }

    // ── English ──────────────────────────────────────────────────────────
    private val enTranslations = mapOf(
        // tired
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "You feel tired today — the goal is just to check off the workout, not to break records. A short, controlled session is enough for maintenance.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Today's goal: keep your usual pace without forcing it. Even 60% of normal intensity counts for long-term maintenance.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Cut weights by 10-15% from normal sessions and focus on proper execution, not numbers.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "With muscle fatigue, you need easily digestible carbs before training — a banana or a little honey can help.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "If the tiredness is general, not just muscular, consider 5-10 minutes of mobility instead of a full workout, just to stay active.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "When tired, avoid testing 1RM — injury risk rises significantly when your nervous system is fatigued.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "Moderate caffeine (a coffee 30-45 min before) can partly offset fatigue if it's early in the day.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "Fatigue cuts explosive strength more than endurance — if you train anyway, pick compound exercises at moderate intensity.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "A strength session missed due to fatigue doesn't ruin progress — your body is asking for recovery, not weakness.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Even an 'average' workout today keeps you on track. Consistency beats perfection in the long run.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "For hypertrophy, volume matters more than max intensity — keep the number of sets, just lower the weight slightly.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "Muscles grow during recovery, not during training. A tired day is a good sign to prioritize sleep over the next nights.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "You don't need to be 'on fire' every session to build muscle — regularity matters more than one intense day.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "If you still choose to train, stop 1-2 reps short of muscular failure — today isn't the day for the limit.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Expect below-average performance today and that's fine — log it as a 'fatigue session' to understand the long-term pattern.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "Even a 20-minute walk or a light workout burns calories and keeps you on pace with your weight-loss goal.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "Don't force an intense workout today just to burn more calories — poor execution from fatigue raises injury risk and would set you back much further.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "For weight loss, a light workout plus controlled eating beats an intense workout followed by overeating from exhaustion.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Today's goal: movement, not maximum burn. Light cardio or a low-weight circuit keeps the calorie deficit without draining you further.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Pick low-impact exercises (walking, band, bike) when tired to protect your joints and recover faster.",
        // exhausted
        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Drink a large glass of water — fatigue is often a sign of mild dehydration.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Take an active recovery day instead of an intense workout — a light walk helps your body recover.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Go to bed 30 minutes earlier tonight and keep a consistent sleep schedule.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Listen to your body — if fatigue persists for several days, it's a signal to reduce training intensity.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Take a short 10–20 minute nap if you can — a quick refresh without hurting your night sleep.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Check your protein intake — a deficit can slow muscle recovery and increase fatigue.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Skip caffeine in the afternoon — it can affect sleep quality even if you fall asleep easily.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Consider a deload week — reduce your training volume by 40–50% to allow full recovery.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Do a few minutes of deep breathing — it activates the parasympathetic nervous system and lowers stress.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Check your magnesium and iron levels — deficiencies in these are common causes of chronic fatigue.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Spend 10–15 minutes outside in natural light — it helps regulate your circadian rhythm.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Avoid screens for at least 30 minutes before bed — blue light disrupts melatonin production.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Cut back on refined sugar — blood sugar spikes followed by crashes amplify feelings of fatigue.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Do 5–10 minutes of light stretching before bed — it relaxes tense muscles and improves sleep.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Avoid overtraining — increasing volume or intensity too fast can lead to exhaustion.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Plan meals to include complex carbohydrates — they provide steady energy without sudden fluctuations.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "Don't skip breakfast — insufficient morning energy intake can worsen fatigue throughout the day.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Cut back on alcohol — even small amounts can affect deep sleep quality.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Prioritize your tasks — mental fatigue adds to physical fatigue and amplifies exhaustion.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "If extreme fatigue persists for more than 2 weeks without a clear cause, see a doctor for a check-up.",

        // normal
        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Keep your usual intensity — a maintenance goal doesn't need progression, just consistency at the same effort level.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Stick to the planned program without adding or cutting volume: maintenance means stability, not experiments.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Use the same weights as last week and check that your form stays correct on every rep.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "A calorie intake equal to your daily burn (no surplus, no deficit) best supports a maintenance goal.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Your energy is normal — it's the ideal moment to follow the program exactly as planned, without adjustments.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "On a normal-energy day you can test linear progression — add 2.5-5% to main lifts if your form stays solid.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "Protein spread over 3-4 meals a day (about 1.6-2g/kg bodyweight) supports long-term strength adaptations.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Normal energy means ideal conditions for heavy compounds (squats, deadlifts, presses) — use it.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "Even on a good day, leave 48h between sessions for the same muscle group at high intensity.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "Strength progress shows over weeks, not sessions — a well-executed normal day is one more brick in the foundation.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "With normal energy, aim for 3-4 sets of 8-12 reps per exercise — the classic hypertrophy range.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "7-9 hours of sleep and steady hydration matter just as much as training for muscle growth.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "Every well-executed normal session adds to your weekly total volume — that's what builds muscle long-term.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "On a normal day, the last 2 reps of each set should be hard to complete without losing form.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Log your weights and reps today — a normal-energy day is the best benchmark for measuring real progress.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "A normal-energy day is perfect for following both your training and nutrition plan without compromises.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Add a higher-intensity interval (short HIIT or circuit) today while you have steady energy for it.",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "A calorie deficit plus regular training, sustained consistently, shows visible results in 4-6 weeks.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Today's goal: keep the planned calorie deficit and finish the full workout with no shortcuts.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Combine weight training with 15-20 minutes of cardio at the end to maximize calorie burn on a steady-energy day.",

        // energetic
        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "You have extra energy — use the surplus for cleaner form, not necessarily more weight, if the goal stays maintenance.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Even if you feel energetic, stick to the maintenance plan — no need to turn the session into a progression workout.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Use the extra energy for controlled tempo (e.g., 3 seconds on the eccentric) instead of adding weight.",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "High energy today is a good sign your nutrition over the past days was balanced — keep the same pace.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Channel the extra energy into a longer warm-up or extra mobility while keeping the main volume unchanged.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "A high-energy day is the right time to test a new max (1RM or 3RM) on a main lift, with a proper warm-up.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Make sure you ate enough carbs beforehand — energy becomes real strength only with sufficient glycogen.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Use today's energy for the heaviest lifts in the program, leaving lighter accessories for another day.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Even with high energy, respect rest times between sets (3-5 min on heavy lifts) — rushing cancels the benefit.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "High-energy days are what really move the strength needle — ride the wave, but listen to your body if form breaks down.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "With high energy you can slightly increase volume (one extra set on main lifts) without hurting the week's recovery.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Even if you feel energetic, don't skip stretching or mobility at the end — it prevents long-term injuries.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "Today's high energy turned into sustained effort is exactly the kind of session that brings visible progress in a few weeks.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Push your main sets close to muscular failure today — your body has the resources for it.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "It's a good day to try a personal record in reps or weight on a secondary exercise, not just the main ones.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "Today's energy is the perfect chance to add a more intense cardio session — you burn more calories without feeling it as hard.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Raise interval intensity (longer sprints or shorter rests) while you're full of energy.",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "An intense session today plus controlled meals can create a bigger calorie deficit than an ordinary day.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Today's goal: use the energy for a full workout (strength + cardio) that maximizes calorie burn.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Alternate weights and cardio circuit-style today — your heart rate stays high and calorie burn increases."
    )

    // ── Russian ──────────────────────────────────────────────────────────
    private val ruTranslations = mapOf(
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "Сегодня ты чувствуешь усталость — цель просто закрыть тренировку, а не бить рекорды. Короткой и контролируемой сессии достаточно для поддержания формы.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Цель на сегодня: сохранить привычный ритм без насилия. Даже 60% от обычной интенсивности имеет значение для долгосрочного поддержания.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Снизь веса на 10-15% от обычных сессий и сосредоточься на правильной технике, а не на цифрах.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "При мышечной усталости перед тренировкой нужны легкоусвояемые углеводы — банан или немного мёда помогут.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "Если усталость общая, а не только мышечная, рассмотри 5-10 минут подвижности вместо полной тренировки — просто чтобы оставаться активным.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "В усталости избегай теста 1ПМ — риск травмы значительно растёт, когда нервная система утомлена.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "Умеренный кофеин (кофе за 30-45 минут) может частично компенсировать усталость, если это начало дня.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "Усталость снижает взрывную силу больше, чем выносливость — если тренируешься, выбирай базовые упражнения умеренной интенсивности.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "Пропущенная из-за усталости силовая сессия не ломает прогресс — твоё тело просит восстановления, а не слабости.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Даже «средняя» тренировка сегодня удерживает тебя в ритме. Постоянство побеждает совершенство в долгосроке.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "Для гипертрофии объём важнее максимальной интенсивности — сохрани число подходов, лишь немного снизив вес.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "Мышцы растут в периоды восстановления, а не на тренировке. Усталый день — хороший сигнал сделать приоритетом сон ближайших ночей.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "Не нужно быть «на огне» каждую сессию, чтобы строить мышечную массу — регулярность важнее одного интенсивного дня.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "Если всё же тренируешься, остановись за 1-2 повтора до мышечного отказа — сегодня не день для предела.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Ожидай сегодня результат ниже среднего — это нормально. Отметь «сессию усталости», чтобы понять долгосрочную картину.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "Даже 20-минутная прогулка или лёгкая тренировка сжигают калории и держат тебя в темпе к цели похудения.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "Не форсируй интенсивную тренировку сегодня только ради лишних калорий — плохая техника из-за усталости повышает риск травмы, которая отбросит тебя намного дальше.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "Для похудения лёгкая тренировка плюс контролируемое питание лучше, чем интенсивная тренировка с последующим перееданием от истощения.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Цель на сегодня: движение, а не максимальное сжигание. Лёгкое кардио или круг с малыми весами сохраняет дефицит калорий, не истощая тебя ещё больше.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Выбирай упражнения с низкой нагрузкой (ходьба, резинка, велосипед), когда устал, чтобы беречь суставы и быстрее восстанавливаться.",

        // exhausted
        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Выпей большой стакан воды — усталость часто является признаком лёгкого обезвоживания.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Устрой день активного восстановления вместо интенсивной тренировки — лёгкая прогулка поможет телу восстановиться.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Ложись спать сегодня на 30 минут раньше и придерживайся постоянного графика сна.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Прислушивайся к своему телу — если усталость сохраняется несколько дней, это сигнал снизить интенсивность тренировок.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Если есть возможность, вздремни 10–20 минут — быстрое восстановление без вреда для ночного сна.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Проверь потребление белка — его нехватка может замедлить восстановление мышц и усилить усталость.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Откажись от кофеина во второй половине дня — он может снизить качество сна, даже если ты легко засыпаешь.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Рассмотри разгрузочную неделю — снизь объём тренировок на 40–50%, чтобы дать телу полностью восстановиться.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Уделите несколько минут глубокому дыханию — это активирует парасимпатическую нервную систему и снижает стресс.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Проверь уровень магния и железа — их дефицит часто становится причиной хронической усталости.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Проведи 10–15 минут на улице при естественном свете — это помогает регулировать циркадный ритм.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Избегай экранов минимум за 30 минут до сна — синий свет нарушает выработку мелатонина.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Сократи потребление рафинированного сахара — резкие скачки сахара в крови усиливают чувство усталости.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Сделай лёгкую растяжку на 5–10 минут перед сном — она расслабляет напряжённые мышцы и улучшает сон.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Избегай перетренированности — слишком быстрое увеличение объёма или интенсивности может привести к истощению.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Планируй приёмы пищи так, чтобы включать сложные углеводы — они дают стабильную энергию без резких колебаний.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "Не пропускай завтрак — недостаточное поступление энергии утром может усилить усталость в течение дня.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Сократи потребление алкоголя — даже небольшие количества могут ухудшить качество глубокого сна.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Расставь приоритеты в задачах — умственная усталость складывается с физической и усиливает истощение.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "Если сильная усталость сохраняется более 2 недель без явной причины, обратись к врачу для обследования.",

        // normal
        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Сохраняй привычную интенсивность — цель поддержания требует не прогрессии, а постоянства на том же уровне усилий.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Придерживайся плана, не добавляя и не убирая объём: поддержание — это стабильность, а не эксперименты.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Используй те же веса, что на прошлой неделе, и проверяй, что техника остаётся правильной в каждом повторе.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "Калорийность, равная дневному расходу (ни профицит, ни дефицит), лучше всего поддерживает цель поддержания.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Твой уровень энергии нормальный — идеальный момент выполнить программу точно как запланировано, без корректировок.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "В день с нормальной энергией можно протестировать линейную прогрессию — добавь 2.5-5% к базовым упражнениям, если техника остаётся стабильной.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "Белок, распределённый на 3-4 приёма пищи в день (около 1.6-2 г/кг тела), поддерживает долгосрочные силовые адаптации.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Нормальная энергия — идеальные условия для тяжёлых базовых упражнений (приседания, тяги, жимы) — используй её.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "Даже в хороший день оставляй 48 часов между сессиями на одну группу мышц при высокой интенсивности.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "Силовой прогресс виден за недели, а не за сессии — хорошо выполненный обычный день это ещё один кирпич в фундаменте.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "С нормальной энергией стремись к 3-4 подходам по 8-12 повторов на упражнение — классический диапазон гипертрофии.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "Сон 7-9 часов и постоянная гидратация важны для роста мышц не меньше, чем тренировка.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "Каждая хорошо выполненная обычная сессия добавляет к недельному объёму — именно это строит мышечную массу в долгосроке.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "В обычный день последние 2 повтора каждого подхода должны даваться тяжело, но без потери техники.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Запиши сегодня веса и повторы — день с нормальной энергией лучший ориентир для измерения реального прогресса.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "День с нормальной энергией идеален, чтобы соблюдать и тренировку, и план питания без компромиссов.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Добавь сегодня интервал повышенной интенсивности (короткое HIIT или круг), пока у тебя есть стабильная энергия для этого.",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "Дефицит калорий плюс регулярные тренировки при постоянстве дают видимые результаты за 4-6 недель.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Цель дня: сохранить запланированный дефицит калорий и завершить полную тренировку без сокращений.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Сочетай силовую с 15-20 минутами кардио в конце, чтобы максимизировать сжигание калорий в день со стабильной энергией.",

        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "У тебя есть лишняя энергия — используй избыток для более чистой техники, а не обязательно для большего веса, если цель — поддержание.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Даже если чувствуешь прилив сил, держись плана поддержания — не нужно превращать сессию в прогрессирующую тренировку.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Используй лишнюю энергию для контролируемого темпа (например, 3 секунды на эксцентрическую фазу) вместо добавления веса.",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "Высокая энергия сегодня — хороший знак, что питание последних дней было сбалансированным — продолжай в том же темпе.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Направь лишнюю энергию в более долгую разминку или дополнительную мобильность, оставив основной объём без изменений.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "День с высокой энергией — подходящий момент протестировать новый максимум (1ПМ или 3ПМ) в базовом упражнении с правильной разминкой.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Убедись, что съел достаточно углеводов — энергия превращается в реальную силу только при достаточном гликогене.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Используй сегодняшнюю энергию на самые тяжёлые упражнения программы, оставив лёгкие вспомогательные на другой день.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Даже с большой энергией соблюдай паузы между подходами (3-5 минут на тяжёлых упражнениях) — спешка отменяет пользу.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "Дни с высокой энергией по-настоящему двигают стрелку силы — лови волну, но слушай тело, если техника рушится.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "С большой энергией можно слегка увеличить объём (один лишний подход на основных упражнениях), не подрывая восстановление недели.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Даже при приливе сил не пропускай стретчинг или мобильность в конце — это предотвращает травмы в долгосроке.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "Сегодняшняя высокая энергия, превращённая в устойчивое усилие, — именно та сессия, которая даёт видимый прогресс через несколько недель.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Доведи сегодня основные подходы почти до мышечного отказа — у твоего тела есть ресурсы для этого.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "Хороший день попробовать личный рекорд в повторах или весе на вспомогательном упражнении, а не только на базовых.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "Сегодняшняя энергия — отличный повод добавить более интенсивное кардио — сжигаешь больше калорий, не чувствуя усилие так тяжело.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Повысь интенсивность интервалов (более длинные спринты или более короткие паузы), пока энергии полный запас.",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "Интенсивная сессия сегодня плюс контролируемое питание могут создать больший дефицит калорий, чем обычный день.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Цель дня: используй энергию для полной тренировки (сила + кардио), максимизирующей сжигание калорий.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Чередуй сегодня упражнения с весами и кардио в формате круга — сердечный ритм остаётся высоким, а сжигание калорий растёт."
    )

    // ── Ukrainian ────────────────────────────────────────────────────────
    private val ukTranslations = mapOf(
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "Сьогодні ти втомлений — мета просто закрити тренування, а не бити рекорди. Короткої та контрольованої сесії достатньо для підтримання форми.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Мета на сьогодні: зберегти звичний ритм без насильства. Навіть 60% від звичайної інтенсивності має значення для довгострокового підтримання.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Знизь ваги на 10-15% від звичайних сесій і зосередься на правильній техніці, а не на цифрах.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "При м'язовій втомі перед тренуванням потрібні легкозасвоювані вуглеводи — банан або трохи меду допоможуть.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "Якщо втома загальна, а не лише м'язова, розглянь 5-10 хвилин мобільності замість повного тренування — просто щоб залишатися активним.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "У втомі уникай тесту 1ПМ — ризик травми значно зростає, коли нервова система втомлена.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "Помірний кофеїн (кава за 30-45 хв) може частково компенсувати втому, якщо це початок дня.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "Втома знижує вибухову силу більше, ніж витривалість — якщо все ж тренуєшся, обирай базові вправи помірної інтенсивності.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "Пропущена через втому силова сесія не ламає прогрес — твоє тіло просить відновлення, а не слабкості.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Навіть «посереднє» тренування сьогодні тримає тебе в ритмі. Постійність перемагає досконалість у довгостроковій перспективі.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "Для гіпертрофії об'єм важливіший за максимальну інтенсивність — збережи кількість підходів, лише трохи знизивши вагу.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "М'язи ростуть у періоди відновлення, а не на тренуванні. Втомлений день — гарний сигнал зробити пріоритетом сон наступних ночей.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "Не потрібно бути «на вогні» кожну сесію, щоб будувати м'язову масу — регулярність важливіша за один інтенсивний день.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "Якщо все ж тренуєшся, зупинись за 1-2 повтори до м'язової відмови — сьогодні не день для межі.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Очікуй сьогодні результат нижче середнього — це нормально. Познач «сесію втоми», щоб зрозуміти довгострокову картину.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "Навіть 20-хвилинна прогулянка чи легке тренування спалюють калорії і тримають тебе в темпі до мети схуднення.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "Не форсуй інтенсивне тренування сьогодні лише заради зайвих калорій — погана техніка через втому підвищує ризик травми, яка відкине тебе набагато далі.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "Для схуднення легке тренування плюс контрольоване харчування краще, ніж інтенсивне тренування з подальшим переїданням від виснаження.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Мета на сьогодні: рух, а не максимальне спалювання. Легке кардіо або коло з малими вагами зберігає дефіцит калорій, не виснажуючи тебе ще більше.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Обирай вправи з низьким навантаженням (ходьба, резинка, велосипед), коли втомлений, щоб берегти суглоби та швидше відновлюватися.",

        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Випий велику склянку води — втома часто є ознакою легкого зневоднення.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Влаштуй день активного відновлення замість інтенсивного тренування — легка прогулянка допоможе тілу відновитися.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Лягай спати сьогодні на 30 хвилин раніше і дотримуйся стабільного графіку сну.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Прислухайся до свого тіла — якщо втома триває кілька днів, це сигнал знизити інтенсивність тренувань.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Якщо є можливість, поспи 10–20 хвилин — швидке відновлення без шкоди для нічного сну.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Перевір споживання білка — його нестача може сповільнити відновлення м'язів і посилити втому.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Відмовся від кофеїну в другій половині дня — він може погіршити якість сну, навіть якщо ти легко засинаєш.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Розглянь розвантажувальний тиждень — знизь обсяг тренувань на 40–50%, щоб дати тілу повністю відновитися.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Приділи кілька хвилин глибокому диханню — це активує парасимпатичну нервову систему і знижує стрес.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Перевір рівень магнію та заліза — їхній дефіцит часто є причиною хронічної втоми.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Проведи 10–15 хвилин надворі при природному світлі — це допомагає регулювати циркадний ритм.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Уникай екранів щонайменше за 30 хвилин до сну — синє світло порушує вироблення мелатоніну.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Скороти вживання рафінованого цукру — стрибки рівня цукру в крові посилюють відчуття втоми.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Зроби легке розтягування на 5–10 хвилин перед сном — воно розслабляє напружені м'язи і покращує сон.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Уникай перетренованості — надто швидке збільшення обсягу або інтенсивності може призвести до виснаження.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Плануй прийоми їжі так, щоб включати складні вуглеводи — вони дають стабільну енергію без різких коливань.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "Не пропускай сніданок — недостатнє надходження енергії вранці може посилити втому протягом дня.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Скороти вживання алкоголю — навіть невеликі кількості можуть погіршити якість глибокого сну.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Розстав пріоритети в завданнях — розумова втома додається до фізичної й посилює виснаження.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "Якщо сильна втома триває понад 2 тижні без явної причини, зверніться до лікаря для обстеження.",

        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Зберігай звичну інтенсивність — мета підтримання потребує не прогресу, а послідовності на тому ж рівні зусиль.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Дотримуйся запланованої програми, не додаючи і не зменшуючи обсяг: підтримання — це стабільність, а не експерименти.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Використовуй ті самі ваги, що минулого тижня, і перевіряй, чи техніка залишається правильною в кожному повторі.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "Калорійність, що дорівнює денному витрату (ні профіцит, ні дефіцит), найкраще підтримує мету підтримання.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Твій рівень енергії нормальний — ідеальний момент виконати програму точно як заплановано, без коригувань.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "У день з нормальною енергією можна протестувати лінійну прогресію — додай 2.5-5% до базових вправ, якщо техніка залишається стабільною.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "Білок, розподілений на 3-4 прийоми їжі на день (близько 1.6-2 г/кг тіла), підтримує довгострокові силові адаптації.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Нормальна енергія — ідеальні умови для важких базових вправ (присідання, тяги, жими) — використай її.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "Навіть у хороший день залишай 48 годин між сесіями на одну групу м'язів при високій інтенсивності.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "Силовий прогрес видно за тижні, а не за сесії — добре виконаний звичайний день це ще одна цеглина у фундаменті.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "З нормальною енергією прагни 3-4 підходів по 8-12 повторів на вправу — класичний діапазон гіпертрофії.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "Сон 7-9 годин і постійна гідратація важливі для росту м'язів не менше, ніж тренування.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "Кожна добре виконана звичайна сесія додає до тижневого обсягу — саме це будує м'язову масу в довгостроковій перспективі.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "У звичайний день останні 2 повтори кожного підходу мають даватися важко, але без втрати техніки.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Запиши сьогодні ваги та повтори — день з нормальною енергією найкращий орієнтир для вимірювання реального прогресу.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "День з нормальною енергією ідеальний, щоб дотримуватися і тренування, і плану харчування без компромісів.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Додай сьогодні інтервал підвищеної інтенсивності (коротке HIIT або коло), поки маєш стабільну енергію для цього.",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "Дефіцит калорій плюс регулярні тренування за сталості дають видимі результати за 4-6 тижнів.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Мета дня: зберегти запланований дефіцит калорій і завершити повне тренування без скорочень.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Поєднай силову з 15-20 хвилинами кардіо наприкінці, щоб максимізувати спалювання калорій у день зі стабільною енергією.",

        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "У тебе є зайва енергія — використай надлишок для чистішої техніки, а не обов'язково для більшої ваги, якщо мета — підтримання.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Навіть якщо почуваєшся енергійно, тримайся плану підтримання — не потрібно перетворювати сесію на прогресуюче тренування.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Використай зайву енергію для контрольованого темпу (наприклад, 3 секунди на ексцентричну фазу) замість додавання ваги.",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "Висока енергія сьогодні — гарний знак, що харчування останніх днів було збалансованим — продовжуй у тому ж темпі.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Спрямуй зайву енергію в довшу розминку або додаткову мобільність, залишивши основний обсяг без змін.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "День з високою енергією — слушний момент протестувати новий максимум (1ПМ або 3ПМ) у базовій вправі з правильною розминкою.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Переконайся, що з'їв достатньо вуглеводів — енергія перетворюється на реальну силу лише при достатньому глікогені.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Використай сьогоднішню енергію на найважчі вправи програми, залишивши легші допоміжні на інший день.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Навіть з великою енергією дотримуйся пауз між підходами (3-5 хв на важких вправах) — поспіх скасовує користь.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "Дні з високою енергією по-справжньому рухають стрілку сили — лови хвилю, але слухай тіло, якщо техніка руйнується.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "З великою енергією можна трохи збільшити обсяг (один зайвий підхід на основних вправах), не підриваючи відновлення тижня.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Навіть при припливі сил не пропускай стретчинг чи мобільність наприкінці — це запобігає травмам у довгостроковій перспективі.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "Сьогоднішня висока енергія, перетворена на стійке зусилля, — саме та сесія, що дає видимий прогрес через кілька тижнів.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Доведи сьогодні основні підходи майже до м'язової відмови — твоє тіло має ресурси для цього.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "Гарний день спробувати особистий рекорд у повторах чи вазі на допоміжній вправі, а не лише на базових.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "Сьогоднішня енергія — чудова нагода додати інтенсивніше кардіо — спалюєш більше калорій, не відчуваючи зусилля так важко.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Підвищ інтенсивність інтервалів (довші спринти або коротші паузи), поки маєш повний запас енергії.",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "Інтенсивна сесія сьогодні плюс контрольоване харчування можуть створити більший дефіцит калорій, ніж звичайний день.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Мета дня: використай енергію для повного тренування (сила + кардіо), що максимізує спалювання калорій.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Чергуй сьогодні вправи з вагами та кардіо у форматі кола — серцевий ритм залишається високим, а спалювання калорій зростає."
    )

    // ── French ───────────────────────────────────────────────────────────
    private val frTranslations = mapOf(
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "Tu te sens fatigué aujourd'hui — l'objectif est juste de cocher la séance, pas de battre des records. Une séance courte et contrôlée suffit pour le maintien.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Objectif du jour : garde ton rythme habituel sans forcer. Même 60 % de l'intensité normale compte pour le maintien à long terme.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Réduis les charges de 10-15 % par rapport aux séances normales et concentre-toi sur la bonne exécution, pas sur les chiffres.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "En cas de fatigue musculaire, tu as besoin de glucides faciles à digérer avant l'entraînement — une banane ou un peu de miel peuvent t'aider.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "Si la fatigue est générale et pas seulement musculaire, envisage 5-10 minutes de mobilité au lieu d'une séance complète, juste pour rester actif.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "Quand tu es fatigué, évite de tester ta 1RM — le risque de blessure augmente fortement quand le système nerveux est épuisé.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "La caféine modérée (un café 30-45 min avant) peut compenser partiellement la fatigue, si c'est tôt dans la journée.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "La fatigue réduit la force explosive plus que l'endurance — si tu t'entraînes quand même, choisis des exercices polyarticulaires à intensité modérée.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "Une séance de force ratée à cause de la fatigue ne ruine pas le progrès — ton corps demande de la récupération, pas de la faiblesse.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Même une séance « moyenne » aujourd'hui te garde dans le rythme. La constance bat la perfection sur le long terme.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "Pour l'hypertrophie, le volume compte plus que l'intensité maximale — garde le nombre de séries, réduis juste un peu la charge.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "Les muscles grandissent pendant la récupération, pas à l'entraînement. Un jour de fatigue est un bon signal pour prioriser le sommeil des nuits suivantes.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "Pas besoin d'être « en feu » à chaque séance pour prendre du muscle — la régularité compte plus qu'une seule journée intense.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "Si tu décides quand même de t'entraîner, arrête-toi 1-2 répétitions avant l'échec musculaire — aujourd'hui n'est pas le jour de la limite.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Attends-toi à une performance sous la moyenne aujourd'hui et c'est ok — note-la comme « séance de fatigue » pour comprendre le schéma à long terme.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "Même une marche de 20 minutes ou une séance légère brûle des calories et te garde dans le rythme vers ton objectif minceur.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "Ne force pas une séance intense aujourd'hui juste pour brûler plus de calories — une mauvaise exécution due à la fatigue augmente le risque de blessure, ce qui te ferait reculer bien plus.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "Pour perdre du poids, une séance légère plus une alimentation contrôlée valent mieux qu'une séance intense suivie de trop manger par épuisement.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Objectif du jour : bouger, pas brûler au maximum. Un cardio léger ou un circuit à petites charges garde le déficit calorique sans t'épuiser davantage.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Choisis des exercices à faible impact (marche, élastique, vélo) quand tu es fatigué, pour protéger tes articulations et récupérer plus vite.",

        // exhausted
        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Bois un grand verre d'eau — la fatigue est souvent un signe de déshydratation légère.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Fais une journée de récupération active plutôt qu'un entraînement intense — une marche légère aide le corps à récupérer.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Couche-toi 30 minutes plus tôt ce soir et garde un horaire de sommeil régulier.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Écoute ton corps — si la fatigue persiste plusieurs jours, c'est le signe qu'il faut réduire l'intensité de l'entraînement.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Fais une courte sieste de 10 à 20 minutes si possible — une recharge rapide sans nuire au sommeil nocturne.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Vérifie ton apport en protéines — un déficit peut ralentir la récupération musculaire et augmenter la fatigue.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Évite la caféine l'après-midi — elle peut nuire à la qualité du sommeil même si tu t'endors facilement.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Envisage une semaine de décharge — réduis ton volume d'entraînement de 40 à 50 % pour permettre une récupération complète.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Fais quelques minutes de respiration profonde — cela active le système nerveux parasympathique et réduit le stress.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Vérifie tes niveaux de magnésium et de fer — leurs carences sont des causes fréquentes de fatigue chronique.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Passe 10 à 15 minutes dehors à la lumière naturelle — cela aide à réguler ton rythme circadien.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Évite les écrans au moins 30 minutes avant de dormir — la lumière bleue perturbe la production de mélatonine.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Réduis le sucre raffiné — les pics de glycémie suivis de chutes brusques amplifient la sensation de fatigue.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Fais 5 à 10 minutes d'étirements légers avant de te coucher — cela détend les muscles tendus et améliore le sommeil.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Évite le surentraînement — augmenter le volume ou l'intensité trop vite peut mener à l'épuisement.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Planifie tes repas en incluant des glucides complexes — ils fournissent une énergie stable sans fluctuations soudaines.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "Ne saute pas le petit-déjeuner — un apport énergétique insuffisant le matin peut aggraver la fatigue tout au long de la journée.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Réduis ta consommation d'alcool — même de petites quantités peuvent nuire à la qualité du sommeil profond.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Priorise tes tâches — la fatigue mentale s'ajoute à la fatigue physique et amplifie l'épuisement.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "Si la fatigue extrême persiste plus de 2 semaines sans cause évidente, consulte un médecin pour un bilan.",

        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Garde ton intensité habituelle — un objectif de maintien ne demande pas de progression, mais de la constance au même niveau d'effort.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Tiens-toi au programme prévu sans ajouter ni retirer de volume : le maintien, c'est de la stabilité, pas des expériences.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Utilise les mêmes charges que la semaine dernière et vérifie que l'exécution reste correcte à chaque répétition.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "Un apport calorique égal à ta dépense quotidienne (ni surplus, ni déficit) soutient le mieux un objectif de maintien.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Ton énergie est normale — c'est le moment idéal pour suivre le programme exactement comme prévu, sans ajustements.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "Un jour d'énergie normale, tu peux tester la progression linéaire — ajoute 2,5-5 % aux exercices de base si ta technique reste solide.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "Des protéines réparties sur 3-4 repas par jour (environ 1,6-2 g/kg de poids) soutiennent les adaptations de force à long terme.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Une énergie normale signifie des conditions idéales pour les exercices polyarticulaires lourds (squats, soulevés, presses) — utilise-la.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "Même lors d'une bonne journée, laisse 48h entre les séances du même groupe musculaire à haute intensité.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "Le progrès en force se voit en semaines, pas en séances — une journée normale bien exécutée est une brique de plus dans la fondation.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "Avec une énergie normale, vise 3-4 séries de 8-12 répétitions par exercice — la fourchette classique de l'hypertrophie.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "7-9 heures de sommeil et une hydratation régulière comptent autant que l'entraînement pour la croissance musculaire.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "Chaque séance normale bien exécutée s'ajoute au volume hebdomadaire total — c'est ce qui construit du muscle à long terme.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "Un jour normal, les 2 dernières répétitions de chaque série devraient être dures à terminer sans perdre la technique.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Note tes charges et répétitions aujourd'hui — un jour d'énergie normale est le meilleur repère pour mesurer le vrai progrès.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "Un jour d'énergie normale est parfait pour suivre à la fois l'entraînement et le plan alimentaire sans compromis.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Ajoute aujourd'hui un intervalle d'intensité plus élevée (HIIT court ou circuit), tant que tu as une énergie stable pour ça.",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "Un déficit calorique plus un entraînement régulier, maintenus avec constance, donnent des résultats visibles en 4-6 semaines.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Objectif du jour : garde le déficit calorique prévu et termine la séance complète, sans raccourcis.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Combine la musculation avec 15-20 minutes de cardio à la fin pour maximiser la dépense calorique lors d'un jour à l'énergie stable.",

        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "Tu as de l'énergie en plus — utilise le surplus pour une exécution plus propre, pas forcément pour plus de charge, si l'objectif reste le maintien.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Même si tu te sens énergique, tiens-toi au plan de maintien — pas besoin de transformer la séance en entraînement de progression.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Utilise l'énergie en plus pour travailler un tempo contrôlé (ex. : 3 secondes sur la phase excentrique) au lieu d'ajouter du poids.",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "Une énergie élevée aujourd'hui est un bon signe que ton alimentation des derniers jours était équilibrée — continue au même rythme.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Canalise l'énergie supplémentaire dans un échauffement plus long ou de la mobilité en plus, en gardant le volume principal inchangé.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "Un jour d'énergie élevée est le bon moment pour tester un nouveau max (1RM ou 3RM) sur un exercice de base, avec un échauffement adapté.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Assure-toi d'avoir mangé assez de glucides avant — l'énergie ne devient une vraie force que si tu as assez de glycogène.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Utilise l'énergie d'aujourd'hui pour les exercices les plus lourds du programme, en laissant les accessoires plus légers pour un autre jour.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Même avec beaucoup d'énergie, respecte les temps de repos entre les séries (3-5 min sur les exercices lourds) — la précipitation annule le bénéfice.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "Les jours d'énergie élevée sont ceux qui font vraiment bouger l'aiguille de la force — profite de la vague, mais écoute ton corps si la technique se dégrade.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "Avec beaucoup d'énergie, tu peux légèrement augmenter le volume (une série de plus sur les exercices principaux) sans compromettre la récupération de la semaine.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Même si tu te sens énergique, ne saute pas l'étirement ou la mobilité à la fin — tu préviens les blessures à long terme.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "L'énergie élevée d'aujourd'hui transformée en effort soutenu est exactement le type de séance qui apporte un progrès visible dans quelques semaines.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Pousse tes séries principales presque jusqu'à l'échec musculaire aujourd'hui — ton corps a les ressources nécessaires.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "C'est un bon jour pour tenter un record personnel en répétitions ou en charge sur un exercice secondaire, pas seulement les exercices de base.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "L'énergie d'aujourd'hui est l'occasion parfaite d'ajouter une séance de cardio plus intense — tu brûles plus de calories sans ressentir l'effort aussi fort.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Augmente l'intensité des intervalles (sprints plus longs ou pauses plus courtes) tant que tu es plein d'énergie.",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "Une séance intense aujourd'hui, combinée à des repas contrôlés, peut créer un déficit calorique plus important qu'un jour ordinaire.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Objectif du jour : profite de l'énergie pour une séance complète (force + cardio) qui maximise la dépense calorique.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Alterne aujourd'hui exercices avec charges et cardio en circuit — le rythme cardiaque reste élevé et la dépense calorique augmente."
    )

    // ── German ───────────────────────────────────────────────────────────
    private val deTranslations = mapOf(
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "Du fühlst dich heute müde — das Ziel ist nur, das Training abzuhaken, nicht Rekorde zu brechen. Eine kurze, kontrollierte Einheit reicht für die Erhaltung.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Ziel von heute: den gewohnten Rhythmus halten, ohne zu forcieren. Selbst 60 % der normalen Intensität zählt für die langfristige Erhaltung.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Reduziere die Gewichte um 10-15 % gegenüber normalen Einheiten und konzentriere dich auf die korrekte Ausführung, nicht auf Zahlen.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "Bei Muskelermüdung brauchst du leicht verdauliche Kohlenhydrate vor dem Training — eine Banane oder etwas Honig können helfen.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "Wenn die Müdigkeit allgemein ist und nicht nur muskulär, erwäge 5-10 Minuten Mobilität statt einer vollen Einheit, nur um aktiv zu bleiben.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "Vermeide bei Müdigkeit den 1RM-Test — das Verletzungsrisiko steigt deutlich, wenn das Nervensystem ermüdet ist.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "Mäßiges Koffein (ein Kaffee 30-45 Min. vorher) kann die Müdigkeit teilweise ausgleichen, wenn es früh am Tag ist.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "Müdigkeit senkt die Explosivkraft mehr als die Ausdauer — wenn du dich doch trainierst, wähle zusammengesetzte Übungen mit moderater Intensität.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "Eine wegen Müdigkeit verpasste Krafteinheit ruiniert den Fortschritt nicht — dein Körper fordert Regeneration, nicht Schwäche.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Selbst ein ‚durchschnittliches' Training hält dich heute im Rhythmus. Beständigkeit schlägt Perfektion auf lange Sicht.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "Für Hypertrophie zählt das Volumen mehr als die maximale Intensität — behalte die Satzzahl, reduziere nur etwas das Gewicht.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "Muskeln wachsen in den Regenerationsphasen, nicht beim Training. Ein müder Tag ist ein gutes Signal, den Schlaf der nächsten Nächte zu priorisieren.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "Du musst nicht bei jeder Einheit ‚on fire' sein, um Muskeln aufzubauen — Regelmäßigkeit zählt mehr als ein einzelner intensiver Tag.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "Wenn du dich doch trainierst, stoppe 1-2 Wiederholungen vor dem Muskelversagen — heute ist nicht der Tag fürs Limit.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Erwarte heute eine unterdurchschnittliche Leistung, und das ist ok — vermerke ‚Ermüdungseinheit', um das Muster langfristig zu verstehen.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "Schon ein 20-minütiger Spaziergang oder ein leichtes Training verbrennt Kalorien und hält dich im Rhythmus deines Abnehmziels.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "Erzwinge heute kein intensives Training nur für mehr Kalorien — schlechte Ausführung durch Müdigkeit erhöht das Verletzungsrisiko und wirft dich viel weiter zurück.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "Zum Abnehmen schlägt ein leichtes Training plus kontrollierte Ernährung ein intensives Training, gefolgt von Überessen aus Erschöpfung.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Ziel von heute: Bewegung, nicht maximale Verbrennung. Ein leichtes Cardio oder ein Kreislauf mit kleinen Gewichten hält das Kaloriendefizit, ohne dich weiter zu erschöpfen.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Wähle bei Müdigkeit gelenkschonende Übungen (Gehen, Band, Fahrrad), um deine Gelenke zu schützen und schneller zu regenerieren.",

        // exhausted
        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Trink ein großes Glas Wasser — Müdigkeit ist oft ein Zeichen leichter Dehydrierung.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Mach einen aktiven Erholungstag statt eines intensiven Trainings — ein leichter Spaziergang hilft dem Körper bei der Erholung.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Geh heute Abend 30 Minuten früher ins Bett und halte einen festen Schlafrhythmus ein.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Höre auf deinen Körper — hält die Müdigkeit mehrere Tage an, ist das ein Signal, die Trainingsintensität zu senken.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Mach wenn möglich ein kurzes 10–20-minütiges Nickerchen — schnelle Erholung, ohne den Nachtschlaf zu beeinträchtigen.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Überprüfe deine Proteinzufuhr — ein Mangel kann die Muskelerholung verlangsamen und Müdigkeit verstärken.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Verzichte am Nachmittag auf Koffein — es kann die Schlafqualität beeinträchtigen, selbst wenn du leicht einschläfst.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Erwäge eine Deload-Woche — reduziere dein Trainingsvolumen um 40–50 %, um vollständige Erholung zu ermöglichen.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Mach ein paar Minuten Tiefenatmung — das aktiviert das parasympathische Nervensystem und senkt Stress.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Überprüfe deinen Magnesium- und Eisenspiegel — ein Mangel ist eine häufige Ursache für chronische Müdigkeit.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Verbringe 10–15 Minuten draußen im natürlichen Licht — das hilft, den zirkadianen Rhythmus zu regulieren.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Vermeide Bildschirme mindestens 30 Minuten vor dem Schlafengehen — blaues Licht stört die Melatoninproduktion.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Reduziere raffinierten Zucker — Blutzuckerspitzen gefolgt von Abstürzen verstärken das Müdigkeitsgefühl.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Mach vor dem Schlafengehen 5–10 Minuten leichtes Dehnen — das entspannt verspannte Muskeln und verbessert den Schlaf.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Vermeide Übertraining — zu schnelles Steigern von Umfang oder Intensität kann zur Erschöpfung führen.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Plane Mahlzeiten mit komplexen Kohlenhydraten — sie liefern gleichmäßige Energie ohne plötzliche Schwankungen.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "Lass das Frühstück nicht aus — zu wenig Energie am Morgen kann die Müdigkeit über den Tag verstärken.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Reduziere Alkoholkonsum — schon geringe Mengen können die Tiefschlafqualität beeinträchtigen.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Setze Prioritäten bei deinen Aufgaben — mentale Erschöpfung addiert sich zur körperlichen und verstärkt die Müdigkeit.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "Wenn extreme Müdigkeit länger als 2 Wochen ohne klaren Grund anhält, lass dich ärztlich untersuchen.",

        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Behalte deine übliche Intensität — ein Erhaltungsziel braucht keine Progression, sondern Beständigkeit auf gleichem Anstrengungsniveau.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Halte dich an das geplante Programm, ohne Volumen hinzuzufügen oder zu reduzieren: Erhaltung bedeutet Stabilität, nicht Experimente.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Nutze dieselben Gewichte wie letzte Woche und prüfe, ob die Ausführung bei jeder Wiederholung korrekt bleibt.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "Eine Kalorienzufuhr, die deinem Tagesverbrauch entspricht (weder Überschuss noch Defizit), unterstützt ein Erhaltungsziel am besten.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Dein Energieniveau ist normal — der ideale Moment, das Programm genau wie geplant ohne Anpassungen durchzuziehen.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "An einem Tag mit normaler Energie kannst du die lineare Progression testen — füge 2,5-5 % bei Grundübungen hinzu, wenn die Technik stabil bleibt.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "Über 3-4 Mahlzeiten verteiltes Protein (ca. 1,6-2 g/kg Körpergewicht) unterstützt langfristige Kraftanpassungen.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Normale Energie bedeutet ideale Bedingungen für schwere Grundübungen (Kniebeugen, Kreuzheben, Drücken) — nutze sie.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "Lass auch an einem guten Tag 48h zwischen Einheiten für dieselbe Muskelgruppe bei hoher Intensität.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "Kraftfortschritt zeigt sich in Wochen, nicht in Einheiten — ein gut ausgeführter normaler Tag ist ein weiterer Baustein im Fundament.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "Mit normaler Energie ziele auf 3-4 Sätze à 8-12 Wiederholungen pro Übung — den klassischen Hypertrophie-Bereich.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "7-9 Stunden Schlaf und stetige Flüssigkeitszufuhr zählen für das Muskelwachstum genauso viel wie das Training.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "Jede gut ausgeführte normale Einheit addiert sich zum wöchentlichen Gesamtvolumen — genau das baut langfristig Muskeln auf.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "An einem normalen Tag sollten die letzten 2 Wiederholungen jedes Satzes schwer zu schaffen sein, ohne die Technik zu verlieren.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Notiere heute Gewichte und Wiederholungen — ein Tag mit normaler Energie ist der beste Maßstab für echten Fortschritt.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "Ein Tag mit normaler Energie ist perfekt, um sowohl Training als auch Ernährungsplan ohne Kompromisse einzuhalten.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Füge heute ein Intervall höherer Intensität hinzu (kurzes HIIT oder Zirkel), solange du konstante Energie dafür hast.",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "Ein Kaloriendefizit plus regelmäßiges Training, konstant durchgehalten, zeigt sichtbare Ergebnisse in 4-6 Wochen.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Ziel des Tages: das geplante Kaloriendefizit halten und das volle Training ohne Abkürzungen abschließen.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Kombiniere Krafttraining mit 15-20 Minuten Cardio am Ende, um die Kalorienverbrennung an einem Tag mit stabiler Energie zu maximieren.",

        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "Du hast Energie übrig — nutze den Überschuss für eine sauberere Ausführung, nicht unbedingt für mehr Gewicht, wenn das Ziel Erhaltung bleibt.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Auch wenn du dich energiegeladen fühlst, bleib beim Erhaltungsplan — du musst die Einheit nicht in ein Progressionstraining verwandeln.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Nutze die Extra-Energie für kontrolliertes Tempo (z. B. 3 Sekunden in der exzentrischen Phase), statt Gewicht hinzuzufügen.",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "Hohe Energie heute ist ein gutes Zeichen, dass deine Ernährung der letzten Tage ausgewogen war — mach im selben Tempo weiter.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Kanalisiere die Extra-Energie in ein längeres Warm-up oder zusätzliche Mobilität, während das Hauptvolumen unverändert bleibt.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "Ein Tag mit hoher Energie ist der richtige Moment, ein neues Maximum (1RM oder 3RM) bei einer Grundübung zu testen, mit passendem Aufwärmen.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Stelle sicher, dass du vorher genug Kohlenhydrate gegessen hast — Energie wird nur mit ausreichend Glykogen zu echter Kraft.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Nutze die Energie von heute für die schwersten Übungen des Programms und lass leichtere Hilfsübungen für einen anderen Tag.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Selbst mit viel Energie halte die Pausenzeiten zwischen den Sätzen ein (3-5 Min. bei schweren Übungen) — Eile macht den Nutzen zunichte.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "Tage mit hoher Energie bewegen die Kraftnadel wirklich — reite die Welle, aber hör auf deinen Körper, wenn die Technik leidet.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "Mit viel Energie kannst du das Volumen leicht erhöhen (ein Satz mehr bei Hauptübungen), ohne die Regeneration der Woche zu gefährden.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Auch wenn du dich energiegeladen fühlst, überspringe Stretching oder Mobilität am Ende nicht — es beugt langfristigen Verletzungen vor.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "Die hohe Energie von heute, in anhaltende Anstrengung umgesetzt, ist genau die Art Einheit, die in ein paar Wochen sichtbaren Fortschritt bringt.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Drück heute deine Hauptsätze bis nahe ans Muskelversagen — dein Körper hat die nötigen Ressourcen dafür.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "Es ist ein guter Tag, einen persönlichen Rekord bei Wiederholungen oder Gewicht an einer Nebenübung zu versuchen, nicht nur an den Grundübungen.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "Die Energie von heute ist die perfekte Gelegenheit für eine intensivere Cardio-Einheit — du verbrennst mehr Kalorien, ohne die Anstrengung so stark zu spüren.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Erhöhe die Intervallintensität (längere Sprints oder kürzere Pausen), solange du volle Energie hast.",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "Eine intensive Einheit heute plus kontrollierte Mahlzeiten kann ein größeres Kaloriendefizit schaffen als ein gewöhnlicher Tag.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Ziel des Tages: nutze die Energie für ein komplettes Training (Kraft + Cardio), das die Kalorienverbrennung maximiert.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Wechsle heute Gewichtsübungen und Cardio im Zirkelformat — der Puls bleibt hoch und die Kalorienverbrennung steigt."
    )

    // ── Spanish ───────────────────────────────────────────────────────────
    private val esTranslations = mapOf(
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "Hoy te sientes cansado — el objetivo es solo completar el entrenamiento, no batir récords. Una sesión corta y controlada basta para el mantenimiento.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Objetivo de hoy: mantener el ritmo habitual sin forzar. Incluso el 60 % de la intensidad normal cuenta para el mantenimiento a largo plazo.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Reduce los pesos un 10-15 % frente a las sesiones normales y concéntrate en la ejecución correcta, no en los números.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "Con fatiga muscular necesitas carbohidratos fáciles de digerir antes del entrenamiento — un plátano o un poco de miel pueden ayudarte.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "Si el cansancio es general y no solo muscular, considera 5-10 minutos de movilidad en lugar de un entrenamiento completo, solo para seguir activo.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "Cuando estás cansado, evita probar el 1RM — el riesgo de lesión aumenta significativamente cuando el sistema nervioso está fatigado.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "La cafeína moderada (un café 30-45 min antes) puede compensar parcialmente la sensación de fatiga, si es temprano en el día.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "La fatiga reduce más la fuerza explosiva que la resistencia — si entrenas igualmente, elige ejercicios compuestos a intensidad moderada.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "Una sesión de fuerza fallida por fatiga no arruina el progreso — tu cuerpo pide recuperación, no debilidad.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Incluso un entrenamiento 'mediocre' hoy te mantiene en ritmo. La constancia vence a la perfección a largo plazo.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "Para la hipertrofia, el volumen importa más que la intensidad máxima — puedes mantener el número de series, solo reduce un poco el peso.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "Los músculos crecen durante la recuperación, no en el entrenamiento. Un día de fatiga es una buena señal para priorizar el sueño de las próximas noches.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "No necesitas estar 'on fire' en cada sesión para construir masa muscular — la regularidad importa más que un solo día intenso.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "Si aun así decides entrenar, detente 1-2 repeticiones antes del fallo muscular — hoy no es el día del límite.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Espera un rendimiento inferior a la media hoy y está bien — anótalo como 'sesión de fatiga' para entender el patrón a largo plazo.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "Incluso un paseo de 20 minutos o un entrenamiento ligero quema calorías y te mantiene en ritmo con tu objetivo de adelgazar.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "No fuerces hoy un entrenamiento intenso solo para quemar más calorías — una mala ejecución por fatiga aumenta el riesgo de lesión, que te haría retroceder mucho más.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "Para adelgazar, un entrenamiento ligero más una alimentación controlada vence a un entrenamiento intenso seguido de comer en exceso por agotamiento.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Objetivo de hoy: movimiento, no quema máxima. Un cardio ligero o un circuito con pesos pequeños mantiene el déficit calórico sin agotarte más.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Elige ejercicios de bajo impacto (caminar, banda, bicicleta) cuando estés cansado, para proteger las articulaciones y recuperarte más rápido.",

        // exhausted
        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Bebe un vaso grande de agua — el cansancio suele ser señal de deshidratación leve.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Haz un día de recuperación activa en lugar de un entrenamiento intenso — una caminata suave ayuda a tu cuerpo a recuperarse.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Acuéstate 30 minutos antes esta noche y mantén un horario de sueño constante.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Escucha a tu cuerpo — si el cansancio persiste durante varios días, es señal de reducir la intensidad del entrenamiento.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Toma una siesta corta de 10-20 minutos si puedes — una recarga rápida sin afectar el sueño nocturno.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Revisa tu ingesta de proteína — un déficit puede ralentizar la recuperación muscular y aumentar el cansancio.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Evita la cafeína por la tarde — puede afectar la calidad del sueño aunque te duermas fácilmente.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Considera una semana de descarga — reduce el volumen de entrenamiento en un 40-50% para permitir la recuperación total.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Haz unos minutos de respiración profunda — activa el sistema nervioso parasimpático y reduce el estrés.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Revisa tus niveles de magnesio y hierro — sus deficiencias son causas comunes de fatiga crónica.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Pasa 10-15 minutos al aire libre con luz natural — ayuda a regular tu ritmo circadiano.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Evita las pantallas al menos 30 minutos antes de dormir — la luz azul altera la producción de melatonina.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Reduce el azúcar refinado — los picos de glucosa seguidos de caídas bruscas intensifican la sensación de cansancio.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Haz 5-10 minutos de estiramientos suaves antes de dormir — relaja los músculos tensos y mejora el sueño.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Evita el sobreentrenamiento — aumentar el volumen o la intensidad demasiado rápido puede provocar agotamiento.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Planifica las comidas para incluir carbohidratos complejos — proporcionan energía constante sin fluctuaciones bruscas.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "No te saltes el desayuno — una ingesta insuficiente de energía por la mañana puede empeorar el cansancio durante el día.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Reduce el consumo de alcohol — incluso pequeñas cantidades pueden afectar la calidad del sueño profundo.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Prioriza tus tareas — el cansancio mental se suma al físico y amplifica el agotamiento.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "Si el cansancio extremo persiste más de 2 semanas sin causa clara, consulta a un médico para una revisión.",

        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Mantén tu intensidad habitual — un objetivo de mantenimiento no pide progresión, sino constancia al mismo nivel de esfuerzo.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Cíñete al programa planificado sin añadir ni quitar volumen: el mantenimiento significa estabilidad, no experimentos.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Usa los mismos pesos que la semana pasada y comprueba que la ejecución sigue siendo correcta en cada repetición.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "Una ingesta calórica igual a tu gasto diario (ni superávit ni déficit) apoya mejor un objetivo de mantenimiento.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Tu nivel de energía es normal — es el momento ideal para seguir el programa exactamente como está planificado, sin ajustes.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "En un día de energía normal puedes probar la progresión lineal — añade 2,5-5 % en los ejercicios básicos si la técnica sigue sólida.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "La proteína repartida en 3-4 comidas al día (aprox. 1,6-2 g/kg de peso) sostiene las adaptaciones de fuerza a largo plazo.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Energía normal significa condiciones ideales para los ejercicios compuestos pesados (sentadillas, peso muerto, press) — úsala.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "Incluso en un buen día, deja 48h entre sesiones del mismo grupo muscular a alta intensidad.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "El progreso de fuerza se ve en semanas, no en sesiones — un día normal bien ejecutado es un ladrillo más en la base.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "Con energía normal, apunta a 3-4 series de 8-12 repeticiones por ejercicio — el rango clásico de hipertrofia.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "Dormir 7-9 horas y la hidratación constante cuentan tanto como el entrenamiento para el crecimiento muscular.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "Cada sesión normal bien ejecutada se suma al volumen semanal total — eso construye masa muscular a largo plazo.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "En un día normal, las últimas 2 repeticiones de cada serie deberían costar completarlas sin perder la técnica.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Anota pesos y repeticiones hoy — un día de energía normal es la mejor referencia para medir el progreso real.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "Un día de energía normal es perfecto para cumplir tanto el entrenamiento como el plan alimentario sin compromisos.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Añade hoy un intervalo de mayor intensidad (HIIT corto o circuito), mientras tengas energía constante para ello.",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "El déficit calórico más entrenamiento regular, sostenidos con constancia, dan resultados visibles en 4-6 semanas.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Objetivo del día: mantener el déficit calórico planificado y completar el entrenamiento entero, sin atajos.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Combina el entrenamiento con pesas con 15-20 minutos de cardio al final para maximizar la quema calórica en un día de energía estable.",

        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "Tienes energía de sobra — usa el excedente para una ejecución más limpia, no necesariamente para más peso, si el objetivo sigue siendo el mantenimiento.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Aunque te sientas enérgico, síguete con el plan de mantenimiento — no hace falta convertir la sesión en un entrenamiento de progresión.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Usa la energía extra para trabajar un tempo controlado (p. ej., 3 segundos en la fase excéntrica) en lugar de añadir peso.",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "La energía alta de hoy es buena señal de que tu alimentación de los últimos días fue equilibrada — continúa al mismo ritmo.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Puedes canalizar la energía extra en un calentamiento más amplio o movilidad adicional, manteniendo el volumen principal sin cambios.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "Un día de energía alta es el momento adecuado para probar un nuevo máximo (1RM o 3RM) en un ejercicio básico, con calentamiento adecuado.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Asegúrate de haber comido suficientes carbohidratos antes — la energía se vuelve fuerza real solo con suficiente glucógeno.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Usa la energía de hoy para los ejercicios más pesados del programa, dejando los accesorios más ligeros para otro día.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Incluso con mucha energía, respeta los tiempos de descanso entre series (3-5 min en ejercicios pesados) — las prisas anulan el beneficio.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "Los días de energía alta son los que realmente mueven la aguja de la fuerza — aprovecha la ola, pero escucha a tu cuerpo si la técnica se rompe.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "Con mucha energía puedes aumentar ligeramente el volumen (una serie más en los ejercicios principales) sin comprometer la recuperación de la semana.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Aunque te sientas enérgico, no te saltes el estiramiento o la movilidad al final — previene lesiones a largo plazo.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "La energía alta de hoy convertida en esfuerzo sostenido es exactamente el tipo de sesión que trae progreso visible en unas semanas.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Empuja hoy tus series principales hasta cerca del fallo muscular — tu cuerpo tiene los recursos necesarios para ello.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "Es un buen día para intentar un récord personal en repeticiones o peso en un ejercicio secundario, no solo en los básicos.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "La energía de hoy es la oportunidad perfecta para añadir un cardio más intenso — quemas más calorías sin sentir el esfuerzo tan pesado.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Sube la intensidad de los intervalos (sprints más largos o pausas más cortas) mientras tengas energía de sobra.",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "Una sesión intensa hoy, combinada con comidas controladas, puede crear un déficit calórico mayor que un día normal.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Objetivo del día: aprovecha la energía para un entrenamiento completo (fuerza + cardio) que maximice la quema calórica.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Alterna hoy ejercicios con pesas y cardio en formato circuito — la frecuencia cardíaca se mantiene alta y la quema calórica aumenta."
    )

    // ── Italian ───────────────────────────────────────────────────────────
    private val itTranslations = mapOf(
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "Oggi ti senti stanco — l'obiettivo è solo spuntare l'allenamento, non battere record. Una sessione breve e controllata basta per il mantenimento.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Obiettivo di oggi: mantenere il ritmo abituale senza forzare. Anche il 60 % dell'intensità normale conta per il mantenimento a lungo termine.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Riduci i pesi del 10-15 % rispetto alle sessioni normali e concentrati sull'esecuzione corretta, non sui numeri.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "Con affaticamento muscolare hai bisogno di carboidrati facilmente digeribili prima dell'allenamento — una banana o un po' di miele possono aiutarti.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "Se la stanchezza è generale e non solo muscolare, valuta 5-10 minuti di mobilità invece di un allenamento completo, solo per restare attivo.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "Quando sei stanco, evita di testare l'1RM — il rischio di infortunio aumenta notevolmente quando il sistema nervoso è affaticato.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "La caffeina moderata (un caffè 30-45 min prima) può compensare parzialmente la stanchezza, se è presto nella giornata.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "La stanchezza riduce la forza esplosiva più della resistenza — se ti alleni comunque, scegli esercizi composti a intensità moderata.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "Una sessione di forza saltata per stanchezza non rovina i progressi — il tuo corpo chiede recupero, non debolezza.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Anche un allenamento 'mediocre' oggi ti tiene nel ritmo. La costanza batte la perfezione a lungo termine.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "Per l'ipertrofia il volume conta più dell'intensità massima — puoi mantenere il numero di serie, riducendo solo un po' il peso.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "I muscoli crescono nei periodi di recupero, non in allenamento. Un giorno di stanchezza è un buon segnale per dare priorità al sonno delle prossime notti.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "Non devi essere 'on fire' a ogni sessione per costruire massa muscolare — la regolarità conta più di un singolo giorno intenso.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "Se decidi comunque di allenarti, fermati 1-2 ripetizioni prima del cedimento muscolare — oggi non è il giorno del limite.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Aspettati oggi una prestazione sotto la media ed è ok — segnala 'sessione di stanchezza' per capire il pattern a lungo termine.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "Anche una passeggiata di 20 minuti o un allenamento leggero brucia calorie e ti tiene nel ritmo del tuo obiettivo dimagrante.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "Non forzare oggi un allenamento intenso solo per bruciare più calorie — una brutta esecuzione per stanchezza aumenta il rischio di infortunio, che ti farebbe fare molti passi indietro.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "Per dimagrire, un allenamento leggero più un'alimentazione controllata battono un allenamento intenso seguito da un'abbuffata per esaurimento.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Obiettivo di oggi: movimento, non bruciare al massimo. Un cardio leggero o un circuito con pesi piccoli mantiene il deficit calorico senza esaurendoti di più.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Scegli esercizi a basso impatto (camminata, banda, bicicletta) quando sei stanco, per proteggere le articolazioni e recuperare più in fretta.",

        // exhausted
        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Bevi un bicchiere grande d'acqua — la stanchezza è spesso segno di lieve disidratazione.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Fai un giorno di recupero attivo invece di un allenamento intenso — una camminata leggera aiuta il corpo a recuperare.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Vai a letto 30 minuti prima stasera e mantieni un orario del sonno costante.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Ascolta il tuo corpo — se la stanchezza persiste per diversi giorni, è un segnale per ridurre l'intensità degli allenamenti.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Fai un breve pisolino di 10-20 minuti se puoi — una ricarica veloce senza compromettere il sonno notturno.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Controlla il tuo apporto proteico — un deficit può rallentare il recupero muscolare e aumentare la stanchezza.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Evita la caffeina nel pomeriggio — può influire sulla qualità del sonno anche se ti addormenti facilmente.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Considera una settimana di scarico — riduci il volume di allenamento del 40-50% per permettere il recupero completo.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Fai qualche minuto di respirazione profonda — attiva il sistema nervoso parasimpatico e riduce lo stress.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Controlla i tuoi livelli di magnesio e ferro — le loro carenze sono cause comuni di stanchezza cronica.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Trascorri 10-15 minuti all'aperto alla luce naturale — aiuta a regolare il ritmo circadiano.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Evita gli schermi per almeno 30 minuti prima di dormire — la luce blu disturba la produzione di melatonina.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Riduci lo zucchero raffinato — i picchi glicemici seguiti da cali improvvisi amplificano la sensazione di stanchezza.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Fai 5-10 minuti di stretching leggero prima di dormire — rilassa i muscoli tesi e migliora il sonno.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Evita l'overtraining — aumentare volume o intensità troppo velocemente può portare all'esaurimento.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Pianifica i pasti includendo carboidrati complessi — forniscono energia costante senza fluttuazioni improvvise.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "Non saltare la colazione — un apporto energetico insufficiente al mattino può peggiorare la stanchezza durante il giorno.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Riduci il consumo di alcol — anche piccole quantità possono influire sulla qualità del sonno profondo.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Dai priorità ai tuoi compiti — la stanchezza mentale si somma a quella fisica e amplifica l'esaurimento.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "Se la stanchezza estrema persiste per più di 2 settimane senza una causa chiara, consulta un medico per un controllo.",

        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Mantieni l'intensità abituale — un obiettivo di mantenimento non chiede progressione, ma costanza allo stesso livello di sforzo.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Attieniti al programma pianificato senza aggiungere o togliere volume: il mantenimento significa stabilità, non esperimenti.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Usa gli stessi pesi della settimana scorsa e verifica che l'esecuzione resti corretta a ogni ripetizione.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "Un apporto calorico uguale al consumo giornaliero (né surplus né deficit) sostiene al meglio un obiettivo di mantenimento.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Il tuo livello di energia è normale — è il momento ideale per seguire il programma esattamente come pianificato, senza aggiustamenti.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "In un giorno con energia normale puoi testare la progressione lineare — aggiungi il 2,5-5 % agli esercizi di base se la tecnica resta solida.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "Le proteine distribuite in 3-4 pasti al giorno (circa 1,6-2 g/kg di peso) sostengono gli adattamenti di forza a lungo termine.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Energia normale significa condizioni ideali per gli esercizi composti pesanti (squat, stacchi, distensioni) — usala.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "Anche in un buon giorno, lascia 48h tra le sessioni per lo stesso gruppo muscolare ad alta intensità.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "Il progresso di forza si vede in settimane, non in sessioni — un giorno normale ben eseguito è un mattone in più nella fondazione.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "Con energia normale, punta a 3-4 serie da 8-12 ripetizioni per esercizio — il range classico per l'ipertrofia.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "Dormire 7-9 ore e un'idratazione costante contano quanto l'allenamento per la crescita muscolare.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "Ogni sessione normale ben eseguita si aggiunge al volume settimanale totale — è questo che costruisce massa muscolare a lungo termine.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "In un giorno normale, le ultime 2 ripetizioni di ogni serie dovrebbero costare fatica da completare senza perdere la tecnica.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Annota pesi e ripetizioni oggi — un giorno di energia normale è il miglior riferimento per misurare il progresso reale.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "Un giorno con energia normale è perfetto per rispettare sia l'allenamento sia il piano alimentare senza compromessi.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Aggiungi oggi un intervallo di intensità più alta (HIIT breve o circuito), finché hai energia costante per farlo.",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "Il deficit calorico più allenamento regolare, sostenuti con costanza, danno risultati visibili in 4-6 settimane.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Obiettivo del giorno: mantenere il deficit calorico pianificato e completare l'allenamento intero, senza scorciatoie.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Combina l'allenamento con pesi con 15-20 minuti di cardio alla fine per massimizzare il consumo calorico in un giorno di energia stabile.",

        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "Hai energia in più — usa il surplus per un'esecuzione più pulita, non necessariamente per più peso, se l'obiettivo resta il mantenimento.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Anche se ti senti energico, attieniti al piano di mantenimento — non serve trasformare la sessione in un allenamento di progressione.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Usa l'energia in più per lavorare sul tempo controllato (es. 3 secondi nella fase eccentrica) invece di aggiungere peso.",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "L'energia alta di oggi è un buon segno che l'alimentazione degli ultimi giorni è stata equilibrata — continua allo stesso ritmo.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Puoi incanalare l'energia extra in un riscaldamento più ampio o mobilità aggiuntiva, mantenendo invariato il volume principale.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "Un giorno con energia alta è il momento giusto per testare un nuovo massimale (1RM o 3RM) su un esercizio di base, con riscaldamento adeguato.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Assicurati di aver mangiato abbastanza carboidrati prima — l'energia diventa forza reale solo con abbastanza glicogeno.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Usa l'energia di oggi per gli esercizi più pesanti del programma, lasciando gli accessori più leggeri per un altro giorno.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Anche con molta energia, rispetta i tempi di recupero tra le serie (3-5 min negli esercizi pesanti) — la fretta annulla il beneficio.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "I giorni di energia alta sono quelli che muovono davvero l'ago della forza — sfrutta l'onda, ma ascolta il corpo se la tecnica crolla.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "Con molta energia puoi aumentare leggermente il volume (una serie in più negli esercizi principali) senza compromettere il recupero della settimana.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Anche se ti senti energico, non saltare stretching o mobilità alla fine — previeni infortuni a lungo termine.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "L'energia alta di oggi trasformata in sforzo costante è esattamente il tipo di sessione che porta progresso visibile tra poche settimane.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Spingi oggi le serie principali quasi fino al cedimento muscolare — il tuo corpo ha le risorse necessarie.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "È un buon giorno per provare un record personale in ripetizioni o peso su un esercizio secondario, non solo su quelli di base.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "L'energia di oggi è l'occasione perfetta per aggiungere un cardio più intenso — bruci più calorie senza sentire lo sforzo altrettanto pesante.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Aumenta l'intensità degli intervalli (sprint più lunghi o pause più brevi) finché hai energia piena.",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "Una sessione intensa oggi, combinata con pasti controllati, può creare un deficit calorico maggiore di un giorno normale.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Obiettivo del giorno: sfrutta l'energia per un allenamento completo (forza + cardio) che massimizzi il consumo calorico.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Alterna oggi esercizi con pesi e cardio in formato circuito — la frequenza cardiaca resta alta e il consumo calorico aumenta."
    )

    // ── Turkish ───────────────────────────────────────────────────────────
    private val trTranslations = mapOf(
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "Bugün kendini yorgun hissediyorsun — amaç sadece antrenmanı tamamlamak, rekor kırmak değil. Kısa ve kontrollü bir seans bakım için yeterli.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Bugünün hedefi: zorlamadan olağan temponu koru. Normal yoğunluğun %60'ı bile uzun vadeli bakım için önemlidir.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Ağırlıkları normal seanslara göre %10-15 düşür ve rakamlara değil doğru uygulamaya odaklan.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "Kas yorgunluğunda antrenmandan önce kolay sindirilen karbonhidratlara ihtiyacın var — bir muz veya biraz bal yardımcı olabilir.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "Yorgunluk sadece kasla ilgili değil genelse, tam antrenman yerine 5-10 dakikalık mobilite düşün — sadece aktif kalmak için.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "Yorgunken 1RM testi yapmaktan kaçın — sinir sistemi yorgunken sakatlanma riski belirgin şekilde artar.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "Ilımlı kafein (30-45 dk önce bir kahve) yorgunluk hissini kısmen telafi edebilir, günün erken saatindeyse.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "Yorgunluk patlayıcı gücü dayanıklılıktan daha çok düşürür — yine de antrenman yapıyorsan orta yoğunlukta bileşik egzersizler seç.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "Yorgunluk yüzünden kaçan bir kuvvet seansı ilerlemeni bozmaz — bedenin zayıflık değil toparlanma istiyor.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Bugün 'vasat' bir antrenman bile seni ritimde tutar. Süreklilik uzun vadede mükemmelliği yener.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "Hipertrofi için hacim maksimum yoğunluktan daha önemlidir — set sayısını koru, sadece ağırlığı biraz düşür.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "Kaslar antrenmanda değil toparlanma dönemlerinde büyür. Yorgun bir gün, sonraki gecelerin uykusuna öncelik vermek için iyi bir işarettir.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "Kas kütlesi inşa etmek için her seansta 'tutuşmaya' gerek yok — düzenlilik tek bir yoğun günden daha önemlidir.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "Yine de antrenman yapmayı seçersen kas yetmezliğinden 1-2 tekrar önce dur — bugün limit günü değil.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Bugün ortalamanın altında bir performans bekle ve bu sorun değil — uzun vadeli düzeni anlamak için 'yorgunluk seansı' olarak not et.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "20 dakikalık bir yürüyüş veya hafif bir antrenman bile kalori yakar ve zayıflama hedefinde seni ritimde tutar.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "Sadece daha çok kalori yakmak için bugün yoğun bir antrenmanı zorlama — yorgunluktan kötü uygulama sakatlanma riskini artırır ve seni çok daha geriye atar.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "Zayıflamak için hafif bir antrenman artı kontrollü beslenme, yoğun antrenman ardından tükenmeden aşırı yemeyi yener.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Bugünün hedefi: maksimum yakma değil hareket. Hafif kardiyo veya küçük ağırlıklı devre, seni daha da tüketmeden kalori açığını korur.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Yorgunken eklemlerini korumak ve daha hızlı toparlanmak için düşük etkili egzersizler seç (yürüyüş, bant, bisiklet).",

        // exhausted
        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Büyük bir bardak su iç — yorgunluk genellikle hafif su kaybının bir işaretidir.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Yoğun bir antrenman yerine aktif dinlenme günü geçir — hafif bir yürüyüş vücudunun toparlanmasına yardımcı olur.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Bu akşam 30 dakika erken yat ve düzenli bir uyku programına sadık kal.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Vücudunu dinle — yorgunluk günlerce sürüyorsa, antrenman yoğunluğunu azaltman gerektiğinin işaretidir.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Mümkünse 10-20 dakikalık kısa bir şekerleme yap — gece uykunu bozmadan hızlı bir yenilenme sağlar.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Protein alımını kontrol et — eksiklik kas toparlanmasını yavaşlatabilir ve yorgunluğu artırabilir.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Öğleden sonra kafeinden kaçın — kolay uykuya dalsan bile uyku kalitesini etkileyebilir.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Bir deload haftası düşün — tam toparlanma için antrenman hacmini %40-50 azalt.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Birkaç dakika derin nefes egzersizi yap — parasempatik sinir sistemini harekete geçirir ve stresi azaltır.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Magnezyum ve demir seviyeni kontrol et — bu minerallerin eksikliği kronik yorgunluğun yaygın nedenlerindendir.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Doğal ışıkta dışarıda 10-15 dakika geçir — bu, sirkadiyen ritmini düzenlemeye yardımcı olur.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Yatmadan en az 30 dakika önce ekranlardan uzak dur — mavi ışık melatonin üretimini bozar.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Rafine şekeri azalt — kan şekeri yükselmeleri ve ardından düşüşler yorgunluk hissini artırır.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Yatmadan önce 5-10 dakika hafif esneme hareketleri yap — gergin kasları gevşetir ve uyku kalitesini artırır.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Aşırı antrenmandan kaçın — hacmi veya yoğunluğu çok hızlı artırmak tükenmişliğe yol açabilir.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Öğünlerini kompleks karbonhidratlar içerecek şekilde planla — ani dalgalanmalar olmadan sürekli enerji sağlarlar.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "Kahvaltıyı atlama — sabah yetersiz enerji alımı gün boyu yorgunluğu artırabilir.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Alkol tüketimini azalt — az miktarda bile derin uyku kalitesini etkileyebilir.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Görevlerini önceliklendir — zihinsel yorgunluk fiziksel yorgunluğa eklenir ve tükenmişliği artırır.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "Aşırı yorgunluk belirgin bir sebep olmadan 2 haftadan uzun sürerse, kontrol için bir doktora görün.",

        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Olağan yoğunluğunu koru — bakım hedefi ilerleme değil, aynı efor seviyesinde tutarlılık ister.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Hacim eklemeden veya azaltmadan planlanan programa bağlı kal: bakım istikrar demektir, deney değil.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Geçen haftaki ağırlıkları kullan ve her tekrarda uygulamanın doğru kaldığını kontrol et.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "Günlük tüketime eşit kalori alımı (ne fazla ne eksik) bir bakım hedefini en iyi destekler.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Enerji seviyen normal — programı ayarlama yapmadan tam olarak planlandığı gibi uygulamak için ideal an.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "Normal enerjili bir günde doğrusal ilerlemeyi test edebilirsin — teknik sağlam kalıyorsa temel egzersizlere %2,5-5 ekle.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "Günde 3-4 öğüne dağıtılmış protein (yaklaşık 1,6-2 g/kg vücut ağırlığı) uzun vadeli kuvvet uyumlarını destekler.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Normal enerji, ağır bileşik egzersizler için (squat, deadlift, press) ideal koşullar demektir — kullan bunu.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "İyi bir günde bile aynı kas grubu için yüksek yoğunlukta seanslar arasında 48 saat bırak.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "Kuvvet ilerlemesi seanslarda değil haftalarda görülür — iyi uygulanan normal bir gün, temele eklenen bir tuğladır.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "Normal enerjiyle egzersiz başına 3-4 set 8-12 tekrar hedefle — hipertrofinin klasik aralığı.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "7-9 saat uyku ve düzenli sıvı alımı, kas büyümesi için antrenman kadar önemlidir.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "İyi uygulanan her normal seans haftalık toplam hacme eklenir — uzun vadede kas kütlesini inşa eden budur.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "Normal bir günde her setin son 2 tekrarı, tekniği kaybetmeden tamamlaması zor olmalı.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Bugün ağırlıkları ve tekrarları not et — normal enerjili bir gün, gerçek ilerlemeyi ölçmek için en iyi referanstır.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "Normal enerjili bir gün, hem antrenmanı hem beslenme planını ödün vermeden uygulamak için mükemmeldir.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Bunun için sabit enerjin varken bugün daha yüksek yoğunluklu bir interval ekle (kısa HIIT veya devre).",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "Kalori açığı artı düzenli antrenman, sürekli sürdürüldüğünde 4-6 haftada görünür sonuçlar verir.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Günün hedefi: planlanan kalori açığını koru ve kestirme yapmadan antrenmanı tamamla.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Stabil enerjili bir günde kalori yakımını en üst düzeye çıkarmak için ağırlık antrenmanını sonda 15-20 dakika kardiyoyla birleştir.",

        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "Fazladan enerjin var — hedef bakım kalıyorsa fazlalığı daha temiz bir uygulama için kullan, illa daha fazla ağırlık için değil.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Kendini enerjik hissetsen bile bakım planına bağlı kal — seansı bir ilerleme antrenmanına dönüştürmene gerek yok.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Fazla enerjiyi ağırlık eklemek yerine kontrollü tempo için kullan (ör. eksantrik fazda 3 saniye).",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "Bugünkü yüksek enerji, son günlerdeki beslenmenin dengeli olduğunun iyi bir işareti — aynı tempoda devam et.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Fazla enerjiyi daha kapsamlı bir ısınmaya veya ek mobiliteye yönlendirebilirsin, ana hacmi değiştirmeden.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "Yüksek enerjili bir gün, uygun ısınmayla temel bir egzersizde yeni maksimumu (1RM veya 3RM) test etmek için doğru zamandır.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Öncesinde yeterince karbonhidrat yediğinden emin ol — enerji, yeterli glikojen varsa gerçek güce dönüşür.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Bugünkü enerjiyi programın en ağır egzersizleri için kullan, daha hafif yardımcıları başka bir güne bırak.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Çok enerjin olsa bile setler arası dinlenme sürelerine uy (ağır egzersizlerde 3-5 dk) — acele etmek faydayı ortadan kaldırır.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "Yüksek enerjili günler güç göstergesini gerçekten hareket ettiren günlerdir — dalgayı yakala, ama teknik bozulursa bedenini dinle.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "Yüksek enerjiyle hacmi hafifçe artırabilirsin (ana egzersizlerde bir set fazla) haftanın toparlanmasını tehlikeye atmadan.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Enerjik hissetsen bile sonda esneme veya mobiliteyi atlama — uzun vadeli sakatlanmaları önler.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "Bugünün yüksek enerjisi sürekli çabaya dönüştüğünde, birkaç hafta içinde görünür ilerleme getiren tam da bu tür bir seanstır.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Bugün ana setlerini kas yetmezliğine yakın it — bedeninin bunun için gerekli kaynakları var.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "Sadece temel egzersizlerde değil, ikincil bir egzersizde de tekrar veya ağırlıkta kişisel rekor denemek için iyi bir gün.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "Bugünkü enerji, daha yoğun bir kardiyo eklemek için mükemmel fırsat — eforu o kadar zor hissetmeden daha çok kalori yakarsın.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Enerjin dolu olduğu sürece interval yoğunluğunu artır (daha uzun sprintler veya daha kısa dinlenmeler).",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "Bugünkü yoğun bir seans, kontrollü öğünlerle birleştiğinde normal bir günden daha büyük bir kalori açığı yaratabilir.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Günün hedefi: kalori yakımını en üst düzeye çıkaran tam bir antrenman (kuvvet + kardiyo) için enerjiyi kullan.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Bugün ağırlık egzersizleri ve kardiyoyu devre formatında dönüşümlü yap — kalp atışı yüksek kalır ve kalori yakımı artar."
    )

    // ── Portuguese ────────────────────────────────────────────────────────
    private val ptTranslations = mapOf(
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "Hoje você se sente cansado — o objetivo é só concluir o treino, não bater recordes. Uma sessão curta e controlada basta para a manutenção.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Objetivo de hoje: manter o ritmo habitual sem forçar. Mesmo 60% da intensidade normal conta para a manutenção a longo prazo.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Reduza os pesos em 10-15% em relação às sessões normais e concentre-se na execução correta, não nos números.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "Com fadiga muscular, você precisa de carboidratos de fácil digestão antes do treino — uma banana ou um pouco de mel podem ajudar.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "Se o cansaço for geral, não apenas muscular, considere 5-10 minutos de mobilidade em vez de um treino completo, só para continuar ativo.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "Quando estiver cansado, evite testar a 1RM — o risco de lesão aumenta significativamente quando o sistema nervoso está fatigado.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "A cafeína moderada (um café 30-45 min antes) pode compensar parcialmente a fadiga, se for cedo no dia.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "A fadiga reduz mais a força explosiva do que a resistência — se treinar mesmo assim, escolha exercícios compostos em intensidade moderada.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "Uma sessão de força perdida por causa do cansaço não estraga o progresso — seu corpo pede recuperação, não fraqueza.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Mesmo um treino 'mediano' hoje mantém você no ritmo. Constância vence perfeição a longo prazo.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "Para hipertrofia, o volume importa mais do que a intensidade máxima — mantenha o número de séries, apenas reduza um pouco o peso.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "Os músculos crescem nos períodos de recuperação, não no treino. Um dia de cansaço é um bom sinal para priorizar o sono das próximas noites.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "Não precisa estar 'on fire' em cada sessão para construir massa muscular — a regularidade importa mais do que um único dia intenso.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "Se ainda assim decidir treinar, pare 1-2 repetições antes da falha muscular — hoje não é dia para o limite.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Espere um desempenho abaixo da média hoje e tudo bem — registre como 'sessão de fadiga' para entender o padrão a longo prazo.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "Mesmo uma caminhada de 20 minutos ou um treino leve queima calorias e mantém você no ritmo do objetivo de emagrecer.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "Não force um treino intenso hoje só para queimar mais calorias — uma execução ruim por cansaço aumenta o risco de lesão, o que o faria regredir muito mais.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "Para emagrecer, um treino leve mais alimentação controlada vence um treino intenso seguido de comer demais por exaustão.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Objetivo de hoje: movimento, não queima máxima. Um cardio leve ou um circuito com pesos pequenos mantém o déficit calórico sem esgotar você ainda mais.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Escolha exercícios de baixo impacto (caminhada, faixa, bicicleta) quando estiver cansado, para proteger as articulações e se recuperar mais rápido.",

        // exhausted
        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Beba um copo grande de água — o cansaço costuma ser sinal de desidratação leve.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Faça um dia de recuperação ativa em vez de um treino intenso — uma caminhada leve ajuda o corpo a se recuperar.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Vá dormir 30 minutos mais cedo hoje e mantenha um horário de sono consistente.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Ouça seu corpo — se o cansaço persistir por vários dias, é um sinal para reduzir a intensidade dos treinos.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Tire uma soneca curta de 10 a 20 minutos, se possível — uma recarga rápida sem prejudicar o sono noturno.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Verifique sua ingestão de proteína — a deficiência pode retardar a recuperação muscular e aumentar o cansaço.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Evite cafeína à tarde — ela pode afetar a qualidade do sono mesmo que você adormeça facilmente.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Considere uma semana de deload — reduza o volume de treino em 40-50% para permitir recuperação total.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Faça alguns minutos de respiração profunda — isso ativa o sistema nervoso parassimpático e reduz o estresse.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Verifique seus níveis de magnésio e ferro — deficiências nesses nutrientes são causas comuns de cansaço crônico.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Passe 10 a 15 minutos ao ar livre na luz natural — isso ajuda a regular seu ritmo circadiano.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Evite telas por pelo menos 30 minutos antes de dormir — a luz azul atrapalha a produção de melatonina.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Reduza o açúcar refinado — os picos de glicose seguidos de quedas bruscas aumentam a sensação de cansaço.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Faça de 5 a 10 minutos de alongamento leve antes de dormir — relaxa músculos tensos e melhora o sono.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Evite o overtraining — aumentar o volume ou a intensidade rápido demais pode levar à exaustão.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Planeje as refeições para incluir carboidratos complexos — eles fornecem energia constante, sem oscilações bruscas.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "Não pule o café da manhã — a ingestão insuficiente de energia pela manhã pode piorar o cansaço ao longo do dia.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Reduza o consumo de álcool — mesmo pequenas quantidades podem afetar a qualidade do sono profundo.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Priorize suas tarefas — o cansaço mental se soma ao físico e amplifica a exaustão.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "Se o cansaço extremo persistir por mais de 2 semanas sem causa clara, consulte um médico para uma avaliação.",

        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Mantenha sua intensidade habitual — um objetivo de manutenção não pede progressão, mas constância no mesmo nível de esforço.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Siga o programa planejado sem adicionar ou cortar volume: manutenção significa estabilidade, não experimentos.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Use os mesmos pesos da semana passada e verifique se a execução continua correta a cada repetição.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "Uma ingestão calórica igual ao seu gasto diário (nem superávit, nem déficit) apoia melhor um objetivo de manutenção.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Seu nível de energia está normal — é o momento ideal para seguir o programa exatamente como planejado, sem ajustes.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "Em um dia de energia normal, você pode testar a progressão linear — adicione 2,5-5% nos exercícios básicos se a técnica continuar sólida.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "Proteína distribuída em 3-4 refeições por dia (cerca de 1,6-2 g/kg de peso) sustenta adaptações de força a longo prazo.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Energia normal significa condições ideais para exercícios compostos pesados (agachamento, terra, supino) — use-a.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "Mesmo em um bom dia, deixe 48h entre sessões para o mesmo grupo muscular em alta intensidade.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "O progresso de força se vê em semanas, não em sessões — um dia normal bem executado é mais um tijolo na fundação.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "Com energia normal, mire 3-4 séries de 8-12 repetições por exercício — a faixa clássica de hipertrofia.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "Dormir 7-9 horas e a hidratação constante contam tanto quanto o treino para o crescimento muscular.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "Cada sessão normal bem executada se soma ao volume semanal total — é isso que constrói massa muscular a longo prazo.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "Em um dia normal, as últimas 2 repetições de cada série devem ser difíceis de completar sem perder a técnica.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Anote pesos e repetições hoje — um dia de energia normal é a melhor referência para medir o progresso real.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "Um dia de energia normal é perfeito para cumprir tanto o treino quanto o plano alimentar sem compromissos.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Adicione hoje um intervalo de maior intensidade (HIIT curto ou circuito), enquanto tiver energia constante para isso.",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "Déficit calórico mais treino regular, mantidos com constância, dão resultados visíveis em 4-6 semanas.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Objetivo do dia: manter o déficit calórico planejado e concluir o treino completo, sem atalhos.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Combine o treino com pesos com 15-20 minutos de cardio no final para maximizar a queima calórica em um dia de energia estável.",

        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "Você tem energia de sobra — use o excedente para uma execução mais limpa, não necessariamente para mais peso, se o objetivo continuar sendo a manutenção.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Mesmo se sentindo enérgico, siga o plano de manutenção — não precisa transformar a sessão em um treino de progressão.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Use a energia extra para trabalhar um tempo controlado (ex.: 3 segundos na fase excêntrica) em vez de adicionar peso.",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "A energia alta de hoje é um bom sinal de que sua alimentação dos últimos dias foi equilibrada — continue no mesmo ritmo.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Você pode canalizar a energia extra para um aquecimento mais amplo ou mobilidade adicional, mantendo o volume principal inalterado.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "Um dia de energia alta é o momento certo para testar um novo máximo (1RM ou 3RM) em um exercício básico, com aquecimento adequado.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Certifique-se de ter comido carboidratos suficientes antes — a energia só vira força real se você tiver glicogênio suficiente.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Use a energia de hoje para os exercícios mais pesados do programa, deixando os acessórios mais leves para outro dia.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Mesmo com muita energia, respeite os tempos de descanso entre as séries (3-5 min em exercícios pesados) — a pressa anula o benefício.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "Os dias de energia alta são os que realmente movem o ponteiro da força — aproveite a onda, mas ouça o corpo se a técnica se romper.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "Com muita energia, você pode aumentar levemente o volume (uma série a mais nos exercícios principais) sem comprometer a recuperação da semana.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Mesmo se sentindo enérgico, não pule o alongamento ou a mobilidade no final — previne lesões a longo prazo.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "A energia alta de hoje transformada em esforço sustentado é exatamente o tipo de sessão que traz progresso visível em algumas semanas.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Empurre hoje suas séries principais quase até a falha muscular — seu corpo tem os recursos necessários para isso.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "É um bom dia para tentar um recorde pessoal em repetições ou peso em um exercício secundário, não só nos básicos.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "A energia de hoje é a oportunidade perfeita para adicionar um cardio mais intenso — você queima mais calorias sem sentir o esforço tão pesado.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Aumente a intensidade dos intervalos (sprints mais longos ou pausas mais curtas) enquanto tiver energia de sobra.",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "Uma sessão intensa hoje, combinada com refeições controladas, pode criar um déficit calórico maior do que um dia comum.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Objetivo do dia: aproveite a energia para um treino completo (força + cardio) que maximize a queima calórica.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Alterne hoje exercícios com pesos e cardio em formato de circuito — a frequência cardíaca permanece alta e a queima calórica aumenta."
    )

    // ── Polish ────────────────────────────────────────────────────────────
    private val plTranslations = mapOf(
        "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere." to
            "Czujesz się dziś zmęczony — celem jest tylko zaliczyć trening, nie bić rekordów. Krótka, kontrolowana sesja wystarczy do utrzymania formy.",
        "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung." to
            "Cel na dziś: utrzymać zwykłe tempo bez forsowania. Nawet 60% normalnej intensywności liczy się do długoterminowego utrzymania.",
        "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre." to
            "Zmniejsz ciężary o 10-15% względem normalnych sesji i skup się na prawidłowym wykonaniu, nie na liczbach.",
        "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta." to
            "Przy zmęczeniu mięśniowym potrzebujesz łatwo przyswajalnych węglowodanów przed treningiem — banan albo odrobina miodu mogą pomóc.",
        "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ." to
            "Jeśli zmęczenie jest ogólne, a nie tylko mięśniowe, rozważ 5-10 minut mobilności zamiast pełnego treningu, tylko po to, by zostać aktywnym.",
        "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit." to
            "Przy zmęczeniu unikaj testowania 1RM — ryzyko kontuzji znacznie rośnie, gdy układ nerwowy jest zmęczony.",
        "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi." to
            "Umiarkowana kofeina (kawa 30-45 min wcześniej) może częściowo zrekompensować zmęczenie, jeśli to wczesna pora dnia.",
        "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată." to
            "Zmęczenie obniża siłę eksplozywną bardziej niż wytrzymałość — jeśli ćwiczysz mimo to, wybierz ćwiczenia złożone o umiarkowanej intensywności.",
        "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune." to
            "Opuszczona przez zmęczenie sesja siłowa nie psuje postępów — twoje ciało prosi o regenerację, nie o słabość.",
        "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung." to
            "Nawet 'przeciętny' trening dziś utrzyma cię w rytmie. Stałość pokonuje perfekcję na dłuższą metę.",
        "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea." to
            "W hipertrofii liczy się objętość bardziej niż maksymalna intensywność — zachowaj liczbę serii, tylko lekko zmniejsz ciężar.",
        "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare." to
            "Mięśnie rosną w okresach regeneracji, nie na treningu. Zmęczony dzień to dobry sygnał, by priorytetem był sen kolejnych nocy.",
        "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă." to
            "Nie musisz być 'on fire' na każdej sesji, by budować masę mięśniową — regularność liczy się bardziej niż jeden intensywny dzień.",
        "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită." to
            "Jeśli mimo wszystko ćwiczysz, zatrzymaj się 1-2 powtórzenia przed zanikiem mięśniowym — dziś nie jest dzień na limit.",
        "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung." to
            "Spodziewaj się dziś wyniku poniżej średniej i to jest ok — zapisz to jako 'sesję zmęczenia', by zrozumieć długoterminowy wzorzec.",
        "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit." to
            "Nawet 20-minutowy spacer albo lekki trening spala kalorie i utrzymuje cię w tempie celu odchudzania.",
        "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi." to
            "Nie wymuszaj dziś intensywnego treningu tylko po to, by spalić więcej kalorii — zła technika przez zmęczenie zwiększa ryzyko kontuzji, która cofnęłaby cię znacznie dalej.",
        "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare." to
            "W odchudzaniu lekki trening plus kontrolowana dieta pokonuje intensywny trening po którym następuje objadanie się z wyczerpania.",
        "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare." to
            "Cel na dziś: ruch, nie maksymalne spalanie. Lekkie cardio lub obwód z małymi ciężarami utrzymuje deficyt kaloryczny, nie wyczerpując cię bardziej.",
        "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede." to
            "Wybieraj ćwiczenia o niskim wpływie (chodzenie, taśma, rower), gdy jesteś zmęczony, by chronić stawy i szybciej się regenerować.",

        // exhausted
        "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară." to
            "Wypij dużą szklankę wody — zmęczenie jest często oznaką lekkiego odwodnienia.",
        "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă." to
            "Zrób dzień aktywnej regeneracji zamiast intensywnego treningu — lekki spacer pomoże ciału się zregenerować.",
        "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant." to
            "Połóż się dziś spać 30 minut wcześniej i trzymaj się stałego harmonogramu snu.",
        "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor." to
            "Słuchaj swojego ciała — jeśli zmęczenie utrzymuje się przez kilka dni, to sygnał, by zmniejszyć intensywność treningów.",
        "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte." to
            "Jeśli możesz, zdrzemnij się przez 10–20 minut — szybkie odświeżenie bez wpływu na sen nocny.",
        "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala." to
            "Sprawdź spożycie białka — jego niedobór może spowolnić regenerację mięśni i zwiększyć zmęczenie.",
        "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor." to
            "Zrezygnuj z kofeiny po południu — może wpływać na jakość snu, nawet jeśli łatwo zasypiasz.",
        "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea." to
            "Rozważ tydzień deload — zmniejsz objętość treningową o 40–50%, aby umożliwić pełną regenerację.",
        "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul." to
            "Poświęć kilka minut na głębokie oddychanie — aktywuje to układ przywspółczulny i redukuje stres.",
        "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice." to
            "Sprawdź poziom magnezu i żelaza — ich niedobory to częste przyczyny przewlekłego zmęczenia.",
        "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian." to
            "Spędź 10–15 minut na zewnątrz w naturalnym świetle — pomaga to regulować rytm dobowy.",
        "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină." to
            "Unikaj ekranów co najmniej 30 minut przed snem — niebieskie światło zaburza produkcję melatoniny.",
        "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală." to
            "Ogranicz cukier rafinowany — skoki poziomu cukru we krwi i ich spadki nasilają uczucie zmęczenia.",
        "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul." to
            "Zrób 5–10 minut lekkiego rozciągania przed snem — rozluźnia napięte mięśnie i poprawia sen.",
        "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare." to
            "Unikaj przetrenowania — zbyt szybkie zwiększanie objętości lub intensywności może prowadzić do wyczerpania.",
        "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște." to
            "Planuj posiłki tak, by zawierały węglowodany złożone — dają stabilną energię bez nagłych wahań.",
        "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei." to
            "Nie pomijaj śniadania — niewystarczająca ilość energii rano może nasilać zmęczenie w ciągu dnia.",
        "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund." to
            "Ogranicz alkohol — nawet niewielkie ilości mogą wpływać na jakość głębokiego snu.",
        "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare." to
            "Uporządkuj zadania według priorytetów — zmęczenie psychiczne dodaje się do fizycznego i wzmacnia wyczerpanie.",
        "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații." to
            "Jeśli silne zmęczenie utrzymuje się dłużej niż 2 tygodnie bez wyraźnej przyczyny, skonsultuj się z lekarzem.",

        "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort." to
            "Utrzymuj zwykłą intensywność — cel utrzymania nie wymaga progresji, lecz konsekwencji na tym samym poziomie wysiłku.",
        "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente." to
            "Trzymaj się zaplanowanego programu bez dodawania i ucinania objętości: utrzymanie to stabilność, nie eksperymenty.",
        "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare." to
            "Używaj tych samych ciężarów co w zeszłym tygodniu i sprawdzaj, czy technika pozostaje poprawna przy każdym powtórzeniu.",
        "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere." to
            "Podaż kaloryczna równa dziennemu wydatkowi (ani nadwyżka, ani deficyt) najlepiej wspiera cel utrzymania.",
        "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări." to
            "Twój poziom energii jest normalny — to idealny moment, by zrealizować program dokładnie tak, jak zaplanowano, bez poprawek.",
        "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă." to
            "W dniu z normalną energią możesz przetestować progresję liniową — dodaj 2,5-5% w ćwiczeniach bazowych, jeśli technika pozostaje solidna.",
        "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung." to
            "Białko rozłożone na 3-4 posiłki dziennie (ok. 1,6-2 g/kg masy ciała) wspiera długoterminowe adaptacje siłowe.",
        "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o." to
            "Normalna energia to idealne warunki do ciężkich ćwiczeń złożonych (przysiady, martwy ciąg, wyciskanie) — wykorzystaj ją.",
        "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare." to
            "Nawet w dobry dzień zostaw 48h między sesjami dla tej samej grupy mięśniowej przy wysokiej intensywności.",
        "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație." to
            "Postęp siłowy widać w tygodniach, nie w sesjach — dobrze wykonany zwykły dzień to kolejna cegła w fundamencie.",
        "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie." to
            "Przy normalnej energii celuj w 3-4 serie po 8-12 powtórzeń na ćwiczenie — klasyczny zakres hipertrofii.",
        "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară." to
            "Sen 7-9 godzin i stałe nawodnienie liczą się dla wzrostu mięśni tak samo jak trening.",
        "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung." to
            "Każda dobrze wykonana zwykła sesja dodaje się do tygodniowej objętości — to właśnie buduje masę mięśniową na dłuższą metę.",
        "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica." to
            "W zwykły dzień ostatnie 2 powtórzenia każdej serii powinny być trudne do dokończenia bez utraty techniki.",
        "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real." to
            "Zapisz dziś ciężary i powtórzenia — dzień z normalną energią to najlepszy punkt odniesienia do mierzenia prawdziwego postępu.",
        "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri." to
            "Dzień z normalną energią jest idealny, by dotrzymać zarówno treningu, jak i planu żywieniowego bez kompromisów.",
        "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta." to
            "Dodaj dziś interwał o wyższej intensywności (krótkie HIIT lub obwód), dopóki masz do tego stałą energię.",
        "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni." to
            "Deficyt kaloryczny plus regularny trening, utrzymywane konsekwentnie, dają widoczne efekty w 4-6 tygodni.",
        "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături." to
            "Cel dnia: utrzymać zaplanowany deficyt kaloryczny i ukończyć cały trening bez skrótów.",
        "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă." to
            "Połącz trening siłowy z 15-20 minutami cardio na końcu, by maksymalizować spalanie kalorii w dniu o stabilnej energii.",

        "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea." to
            "Masz nadmiar energii — wykorzystaj go na czystsze wykonanie, niekoniecznie na większy ciężar, jeśli celem pozostaje utrzymanie.",
        "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie." to
            "Nawet jeśli czujesz się energicznie, trzymaj się planu utrzymania — nie musisz zamieniać sesji w trening progresji.",
        "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate." to
            "Wykorzystaj nadmiar energii na kontrolowane tempo (np. 3 sekundy w fazie ekscentrycznej) zamiast dodawać ciężar.",
        "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm." to
            "Wysoka energia dziś to dobry znak, że dieta ostatnich dni była zbilansowana — kontynuuj w tym samym tempie.",
        "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat." to
            "Możesz skierować nadmiar energii na dłuższą rozgrzewkę lub dodatkową mobilność, pozostawiając główną objętość bez zmian.",
        "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare." to
            "Dzień z wysoką energią to właściwy moment, by przetestować nowe maksimum (1RM lub 3RM) w ćwiczeniu bazowym, z odpowiednią rozgrzewką.",
        "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient." to
            "Upewnij się, że zjadłeś wystarczająco węglowodanów — energia zamienia się w prawdziwą siłę tylko przy wystarczającym glikogenie.",
        "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi." to
            "Wykorzystaj dzisiejszą energię na najcięższe ćwiczenia programu, zostawiając lżejsze dodatki na inny dzień.",
        "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul." to
            "Nawet przy dużej energii przestrzegaj czasów odpoczynku między seriami (3-5 min przy ciężkich ćwiczeniach) — pośpiech niweluje korzyść.",
        "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică." to
            "Dni z wysoką energią naprawdę przesuwają wskazówkę siły — łap falę, ale słuchaj ciała, gdy technika się psuje.",
        "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii." to
            "Przy dużej energii możesz lekko zwiększyć objętość (jedna seria więcej przy głównych ćwiczeniach) bez szkody dla tygodniowej regeneracji.",
        "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung." to
            "Nawet gdy czujesz się energicznie, nie pomijaj stretchingu czy mobilności na końcu — zapobiega to kontuzjom na dłuższą metę.",
        "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni." to
            "Dzisiejsza wysoka energia zamieniona w stały wysiłek to dokładnie ten rodzaj sesji, który przynosi widoczny postęp za kilka tygodni.",
        "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta." to
            "Doprowadź dziś główne serie blisko zaniku mięśniowego — twoje ciało ma do tego potrzebne zasoby.",
        "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază." to
            "To dobry dzień, by spróbować rekordu osobistego w powtórzeniach lub ciężarze na ćwiczeniu drugorzędnym, nie tylko na bazowych.",
        "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu." to
            "Dzisiejsza energia to idealna okazja, by dodać intensywniejsze cardio — spalasz więcej kalorii, nie czując wysiłku tak mocno.",
        "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin." to
            "Zwiększ intensywność interwałów (dłuższe sprinty lub krótsze pauzy), dopóki masz pełno energii.",
        "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită." to
            "Intensywna sesja dziś plus kontrolowane posiłki mogą stworzyć większy deficyt kaloryczny niż zwykły dzień.",
        "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică." to
            "Cel dnia: wykorzystaj energię na pełny trening (siła + cardio), który maksymalizuje spalanie kalorii.",
        "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește." to
            "Przeplataj dziś ćwiczenia z ciężarami i cardio w formie obwodu — tętno pozostaje wysokie, a spalanie kalorii rośnie."
    )
}
