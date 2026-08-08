package com.example.kinetic

/**
 * Static bank of fitness tips, sourced from tips_kinetic.json.
 *
 * Each tip is tagged with an energy level (obosit/normal/energic) and an
 * objective (mentinere/forta/masa_musculara/slabit), plus a category.
 * The bank is the primary, offline, free source of tips. Selection logic
 * follows tips_selection_strategy.md: filter by the user's current
 * energy level + objective, exclude recently shown ids, pick randomly from
 * the remainder, and reset the pool for that combination only when it's
 * exhausted (round-robin).
 */
data class KineticTip(
    val id: String,
    val energyLevel: String, // obosit | normal | energic
    val objective: String,   // mentinere | forta | masa_musculara | slabit
    val category: String,    // performanta | sfat_obiectiv | sfat_tehnic | nutritie | energie | recuperare | motivatie | forteaza_te
    val text: String
)

object TipsBank {

    /** Maps the app's onboarding goal keys to the bank's objective keys. */
    fun objectiveKeyForGoal(goal: String): String = when (goal) {
        "strength" -> "forta"
        "mass" -> "masa_musculara"
        "weight_loss" -> "slabit"
        "maintenance" -> "mentinere"
        else -> "mentinere"
    }

    /**
     * Maps the dashboard mood to the bank's energy keys.
     * 0 = epuizat (exhausted), 1 = obosit (tired), 2 = normal, 3 = energic.
     */
    fun energyKeyForMood(mood: Int): String = when (mood) {
        0 -> "epuizat"
        1 -> "obosit"
        3 -> "energic"
        else -> "normal"
    }

    /**
     * Core selection algorithm (see tips_selection_strategy.md §1).
     *
     * - Pools only tips matching the current energyLevel + objective.
     * - Excludes ids present in [recentlyShown] (sliding window).
     * - If fewer than [count] remain, falls back to the full pool (round-robin reset).
     * - Returns [count] random tips from the candidates.
     */
    fun getNextTips(
        energyLevel: String,
        objective: String,
        count: Int = 3,
        recentlyShown: Set<String> = emptySet()
    ): List<KineticTip> {
        val pool = allTips.filter { it.energyLevel == energyLevel && it.objective == objective }
        if (pool.isEmpty()) return emptyList()
        // Always prefer tips NOT shown recently; only top up with the rest of the
        // pool when there aren't enough fresh ones (round-robin reset per combo).
        val available = pool.filterNot { it.id in recentlyShown }
        val candidates = if (available.size >= count) available else (available + pool).distinct()
        return candidates.shuffled().take(count)
    }

    val allTips: List<KineticTip> = listOf(
        // ── epuizat ────────────────────────────────────────────────────────
        KineticTip("epuizat-01", "epuizat", "mentinere", "recuperare", "Bea un pahar mare cu apă — oboseala este adesea un semn de deshidratare ușoară."),
        KineticTip("epuizat-02", "epuizat", "mentinere", "recuperare", "Ia o zi de recuperare activă în loc de un antrenament intens — o plimbare ușoară ajută corpul să se refacă."),
        KineticTip("epuizat-03", "epuizat", "mentinere", "energie", "Culcă-te cu 30 de minute mai devreme în seara asta și menține un program de somn constant."),
        KineticTip("epuizat-04", "epuizat", "mentinere", "sfat_obiectiv", "Ascultă-ți corpul — dacă oboseala persistă mai multe zile, este un semnal să reduci intensitatea antrenamentelor."),
        KineticTip("epuizat-05", "epuizat", "mentinere", "motivatie", "Fă un pui de somn scurt, de 10-20 de minute, dacă ai posibilitatea — refresh rapid fără a afecta somnul de noapte."),

        KineticTip("epuizat-06", "epuizat", "forta", "nutritie", "Verifică-ți aportul de proteine — un deficit poate încetini recuperarea musculară și crește oboseala."),
        KineticTip("epuizat-07", "epuizat", "forta", "sfat_tehnic", "Ia o pauză de la cafeină după-amiaza — poate afecta calitatea somnului chiar dacă adormi ușor."),
        KineticTip("epuizat-08", "epuizat", "forta", "recuperare", "Ia în calcul o săptămână de deload — reduce volumul antrenamentelor cu 40-50% pentru a permite refacerea."),
        KineticTip("epuizat-09", "epuizat", "forta", "energie", "Fă câteva minute de respirație profundă — activează sistemul nervos parasimpatic și reduce stresul."),
        KineticTip("epuizat-10", "epuizat", "forta", "recuperare", "Verifică nivelul de magneziu și fier — deficiențele lor sunt cauze frecvente ale oboselii cronice."),

        KineticTip("epuizat-11", "epuizat", "masa_musculara", "recuperare", "Petrece 10-15 minute afară, la lumina naturală — ajută la reglarea ritmului circadian."),
        KineticTip("epuizat-12", "epuizat", "masa_musculara", "sfat_tehnic", "Evită ecranele cu cel puțin 30 de minute înainte de culcare — lumina albastră perturbă producția de melatonină."),
        KineticTip("epuizat-13", "epuizat", "masa_musculara", "nutritie", "Redu zahărul rafinat — vârfurile de glicemie urmate de scăderi bruște amplifică senzația de oboseală."),
        KineticTip("epuizat-14", "epuizat", "masa_musculara", "recuperare", "Fă un stretching ușor de 5-10 minute înainte de culcare — relaxează mușchii tensionați și îmbunătățește somnul."),
        KineticTip("epuizat-15", "epuizat", "masa_musculara", "performanta", "Evită supraîncărcarea la sală — creșterea prea rapidă a volumului sau intensității poate duce la epuizare."),

        KineticTip("epuizat-16", "epuizat", "slabit", "nutritie", "Planifică mesele astfel încât să incluzi carbohidrați complecși — oferă energie constantă, fără fluctuații bruște."),
        KineticTip("epuizat-17", "epuizat", "slabit", "nutritie", "Nu sări peste micul dejun — un aport insuficient de energie dimineața poate accentua oboseala pe tot parcursul zilei."),
        KineticTip("epuizat-18", "epuizat", "slabit", "recuperare", "Redu consumul de alcool — chiar și cantități mici pot afecta calitatea somnului profund."),
        KineticTip("epuizat-19", "epuizat", "slabit", "motivatie", "Organizează-ți sarcinile pe priorități — oboseala mentală se adaugă la cea fizică și amplifică senzația de epuizare."),
        KineticTip("epuizat-20", "epuizat", "slabit", "sfat_obiectiv", "Dacă oboseala extremă persistă peste 2 săptămâni fără o cauză clară, consultă un medic pentru investigații."),

        // ── obosit ─────────────────────────────────────────────────────────
        KineticTip("obosit-01", "obosit", "mentinere", "performanta", "Azi te simți obosit — obiectivul e doar să bifezi antrenamentul, nu să bați recorduri. O sesiune scurtă și controlată e suficientă pentru menținere."),
        KineticTip("obosit-02", "obosit", "mentinere", "sfat_obiectiv", "Scopul de azi: menține ritmul obișnuit fără să forțezi. Chiar și 60% din intensitatea normală contează pentru menținere pe termen lung."),
        KineticTip("obosit-03", "obosit", "mentinere", "sfat_tehnic", "Redu greutățile cu 10-15% față de sesiunile normale și concentrează-te pe execuția corectă, nu pe cifre."),
        KineticTip("obosit-04", "obosit", "mentinere", "nutritie", "La oboseală musculară, ai nevoie de carbohidrați ușor digerabili înainte de antrenament — o banană sau puțină miere te pot ajuta."),
        KineticTip("obosit-05", "obosit", "mentinere", "energie", "Dacă oboseala e generală, nu doar musculară, ia în calcul 5-10 minute de mobilitate în loc de antrenament complet, doar ca să rămâi activ."),
        KineticTip("obosit-06", "obosit", "forta", "sfat_tehnic", "La oboseală, evită să testezi 1RM — riscul de accidentare crește semnificativ când sistemul nervos e obosit."),
        KineticTip("obosit-07", "obosit", "forta", "nutritie", "Cofeina moderată (o cafea cu 30-45 min înainte) poate compensa parțial senzația de oboseală, dacă e devreme în zi."),
        KineticTip("obosit-08", "obosit", "forta", "energie", "Oboseala scade forța explozivă mai mult decât rezistența — dacă tot te antrenezi, alege exerciții compuse la intensitate moderată."),
        KineticTip("obosit-09", "obosit", "forta", "recuperare", "O sesiune de forță ratată din cauza oboselii nu strică progresul — corpul tău îți cere recuperare, nu slăbiciune."),
        KineticTip("obosit-10", "obosit", "forta", "motivatie", "Chiar și un antrenament „mediocru” azi te ține în ritm. Constanța bate perfecțiunea pe termen lung."),
        KineticTip("obosit-11", "obosit", "masa_musculara", "energie", "Pentru hipertrofie, volumul contează mai mult decât intensitatea maximă — poți păstra numărul de serii, doar redu puțin greutatea."),
        KineticTip("obosit-12", "obosit", "masa_musculara", "recuperare", "Mușchii cresc în perioadele de recuperare, nu la antrenament. O zi de oboseală e un semnal bun să prioritizezi somnul din nopțile următoare."),
        KineticTip("obosit-13", "obosit", "masa_musculara", "motivatie", "Nu e nevoie să fii „on fire” la fiecare sesiune ca să construiești masă musculară — regularitatea contează mai mult decât o singură zi intensă."),
        KineticTip("obosit-14", "obosit", "masa_musculara", "forteaza_te", "Dacă alegi totuși să te antrenezi, oprește-te cu 1-2 repetări înainte de eșec muscular — azi nu e ziua pentru limită."),
        KineticTip("obosit-15", "obosit", "masa_musculara", "performanta", "Așteaptă-te la o performanță sub medie azi și e ok — notează „sesiune de oboseală” ca să înțelegi tiparul pe termen lung."),
        KineticTip("obosit-16", "obosit", "slabit", "motivatie", "Chiar și o plimbare de 20 de minute sau un antrenament ușor ard calorii și te mențin în ritm cu obiectivul de slăbit."),
        KineticTip("obosit-17", "obosit", "slabit", "forteaza_te", "Nu forța un antrenament intens azi doar ca să arzi mai multe calorii — o execuție proastă din oboseală crește riscul de accidentare, care te-ar da mult mai mult înapoi."),
        KineticTip("obosit-18", "obosit", "slabit", "performanta", "Pentru slăbit, un antrenament ușor plus o alimentație controlată bat un antrenament intens urmat de mâncat excesiv din epuizare."),
        KineticTip("obosit-19", "obosit", "slabit", "sfat_obiectiv", "Obiectivul de azi: mișcare, nu ardere maximă. Un cardio ușor sau un circuit cu greutăți mici păstrează deficitul caloric fără să te epuizeze și mai tare."),
        KineticTip("obosit-20", "obosit", "slabit", "sfat_tehnic", "Alege exerciții cu impact redus (mers, bandă, bicicletă) când ești obosit, ca să protejezi articulațiile și să te recuperezi mai repede."),

        // ── normal ─────────────────────────────────────────────────────────
        KineticTip("normal-01", "normal", "mentinere", "performanta", "Menține intensitatea obișnuită — obiectivul de menținere nu cere progresie, ci consecvență la același nivel de efort."),
        KineticTip("normal-02", "normal", "mentinere", "sfat_obiectiv", "Ține-te de programul planificat fără să adaugi sau să scazi volum: menținerea înseamnă stabilitate, nu experimente."),
        KineticTip("normal-03", "normal", "mentinere", "sfat_tehnic", "Folosește aceleași greutăți ca săptămâna trecută și verifică dacă execuția rămâne corectă la fiecare repetare."),
        KineticTip("normal-04", "normal", "mentinere", "nutritie", "Un aport caloric egal cu consumul zilnic (nici surplus, nici deficit) susține cel mai bine un obiectiv de menținere."),
        KineticTip("normal-05", "normal", "mentinere", "energie", "Nivelul tău de energie e normal — e momentul ideal să respecți programul exact așa cum e planificat, fără ajustări."),
        KineticTip("normal-06", "normal", "forta", "sfat_tehnic", "Într-o zi cu energie normală, poți testa progresia liniară — adaugă 2.5-5% la exercițiile de bază dacă tehnica rămâne solidă."),
        KineticTip("normal-07", "normal", "forta", "nutritie", "Proteina distribuită în 3-4 mese pe zi (aprox. 1.6-2g/kg corp) susține adaptările de forță pe termen lung."),
        KineticTip("normal-08", "normal", "forta", "energie", "Energie normală înseamnă condiții ideale pentru exercițiile compuse grele (genuflexiuni, îndreptări, împins) — folosește-o."),
        KineticTip("normal-09", "normal", "forta", "recuperare", "Chiar și într-o zi bună, lasă 48h între sesiunile pentru același grup muscular la intensitate mare."),
        KineticTip("normal-10", "normal", "forta", "motivatie", "Progresul în forță se vede în săptămâni, nu în sesiuni — o zi normală bine executată e o cărămidă în plus la fundație."),
        KineticTip("normal-11", "normal", "masa_musculara", "energie", "Cu energie normală, țintește 3-4 serii de 8-12 repetări pe exercițiu — intervalul clasic pentru hipertrofie."),
        KineticTip("normal-12", "normal", "masa_musculara", "recuperare", "Somnul de 7-9 ore și hidratarea constantă contează la fel de mult ca antrenamentul pentru creșterea musculară."),
        KineticTip("normal-13", "normal", "masa_musculara", "motivatie", "Fiecare sesiune normală, bine executată, se adaugă la volumul total săptămânal — asta construiește masă musculară pe termen lung."),
        KineticTip("normal-14", "normal", "masa_musculara", "forteaza_te", "Într-o zi normală, ultimele 2 repetări din fiecare serie ar trebui să fie greu de dus fără să pierzi tehnica."),
        KineticTip("normal-15", "normal", "masa_musculara", "performanta", "Notează greutățile și repetările azi — o zi de energie normală e cel mai bun reper pentru a măsura progresul real."),
        KineticTip("normal-16", "normal", "slabit", "motivatie", "O zi cu energie normală e perfectă pentru a respecta atât antrenamentul cât și planul alimentar fără compromisuri."),
        KineticTip("normal-17", "normal", "slabit", "forteaza_te", "Adaugă un interval de intensitate mai mare (HIIT scurt sau circuit) azi, cât ai energie constantă pentru asta."),
        KineticTip("normal-18", "normal", "slabit", "performanta", "Deficitul caloric plus antrenament regulat, susținute constant, dau rezultate vizibile în 4-6 săptămâni."),
        KineticTip("normal-19", "normal", "slabit", "sfat_obiectiv", "Obiectivul zilei: menține deficitul caloric planificat și finalizează antrenamentul complet, fără scurtături."),
        KineticTip("normal-20", "normal", "slabit", "sfat_tehnic", "Combină antrenamentul cu greutăți cu 15-20 minute de cardio la final pentru a maximiza arderea calorică într-o zi cu energie stabilă."),

        // ── energic ─────────────────────────────────────────────────────────
        KineticTip("energic-01", "energic", "mentinere", "performanta", "Ai energie în plus — poți folosi surplusul pentru o execuție mai curată, nu neapărat pentru mai multă greutate, dacă scopul rămâne menținerea."),
        KineticTip("energic-02", "energic", "mentinere", "sfat_obiectiv", "Chiar dacă te simți energic, ține-te de planul de menținere — nu e nevoie să transformi sesiunea într-un antrenament de progresie."),
        KineticTip("energic-03", "energic", "mentinere", "sfat_tehnic", "Folosește energia în plus pentru a lucra tempo-ul controlat (ex: 3 secunde pe faza excentrică) în loc să adaugi greutate."),
        KineticTip("energic-04", "energic", "mentinere", "nutritie", "Energia ridicată azi e un semn bun că alimentația din ultimele zile a fost echilibrată — continuă în același ritm."),
        KineticTip("energic-05", "energic", "mentinere", "energie", "Poți canaliza energia extra într-un warm-up mai amplu sau mobilitate suplimentară, păstrând volumul principal neschimbat."),
        KineticTip("energic-06", "energic", "forta", "sfat_tehnic", "O zi cu energie ridicată e momentul potrivit să testezi un nou maxim (1RM sau 3RM) la un exercițiu de bază, cu încălzire corespunzătoare."),
        KineticTip("energic-07", "energic", "forta", "nutritie", "Asigură-te că ai mâncat suficienți carbohidrați înainte — energia se traduce în forță reală doar dacă ai glicogen suficient."),
        KineticTip("energic-08", "energic", "forta", "energie", "Folosește energia de azi pentru exercițiile cele mai grele din program, lăsând accesoriile mai ușoare pentru altă zi."),
        KineticTip("energic-09", "energic", "forta", "recuperare", "Chiar și cu energie mare, respectă timpii de pauză între serii (3-5 min la exercițiile grele) — graba anulează beneficiul."),
        KineticTip("energic-10", "energic", "forta", "motivatie", "Zilele cu energie ridicată sunt cele care mută cu adevărat acul forței — profită de val, dar ascultă corpul dacă tehnica se strică."),
        KineticTip("energic-11", "energic", "masa_musculara", "energie", "Cu energie mare, poți crește ușor volumul (o serie în plus la exercițiile principale) fără să compromiți recuperarea săptămânii."),
        KineticTip("energic-12", "energic", "masa_musculara", "recuperare", "Chiar dacă te simți energic, nu sări peste stretching sau mobilitate la final — previi accidentările pe termen lung."),
        KineticTip("energic-13", "energic", "masa_musculara", "motivatie", "Energia ridicată de azi transformată în efort susținut e exact tipul de sesiune care aduce progres vizibil peste câteva săptămâni."),
        KineticTip("energic-14", "energic", "masa_musculara", "forteaza_te", "Împinge seriile principale până aproape de eșec muscular azi — corpul tău are resursele necesare pentru asta."),
        KineticTip("energic-15", "energic", "masa_musculara", "performanta", "E o zi bună să încerci un record personal la repetări sau greutate pe un exercițiu secundar, nu doar la cele de bază."),
        KineticTip("energic-16", "energic", "slabit", "motivatie", "Energia de azi e ocazia perfectă să adaugi un antrenament cardio mai intens — arzi mai multe calorii fără să simți efortul la fel de greu."),
        KineticTip("energic-17", "energic", "slabit", "forteaza_te", "Crește intensitatea intervalelor (sprinturi mai lungi sau pauze mai scurte) cât timp ai energie din plin."),
        KineticTip("energic-18", "energic", "slabit", "performanta", "O sesiune intensă azi, combinată cu masa alimentară controlată, poate crea un deficit caloric mai mare decât o zi obișnuită."),
        KineticTip("energic-19", "energic", "slabit", "sfat_obiectiv", "Obiectivul zilei: profită de energie pentru un antrenament complet (forță + cardio) care maximizează arderea calorică."),
        KineticTip("energic-20", "energic", "slabit", "sfat_tehnic", "Alternează exerciții cu greutăți și cardio în format de circuit azi — ritmul cardiac rămâne ridicat, iar arderea calorică crește.")
    )
}
