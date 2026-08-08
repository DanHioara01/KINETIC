package com.example.kinetic

import android.content.Context
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Populează tabela `food_items` cu alimente comune (offline).
 * Valorile sunt per 100g. Pentru alimente numărate pe bucăți (PIECE),
 * `gramsPerPiece` spune cât cântărește o bucată tipică, iar valorile reale
 * per bucată se calculează din cele per 100g la runtime.
 *
 * Fiecare aliment are numele tradus în toate limbile aplicației
 * (ro, en, ru, uk, fr, de, es, it, tr, pt, pl) — câmpurile name*.
 *
 * Seed-ul este versionat (SharedPreferences): când lista de produse crește,
 * bump-uim SEED_VERSION și baza se re-sedează automat și pe instalările vechi.
 */
class FoodItemSeeder(private val db: AppDatabase, private val context: Context) {

    /**
     * Seedează baza dacă versiunea din prefs e mai veche decât SEED_VERSION.
     * Resetează tabelul — sigur, pentru că jurnalul (food_entries) salvează
     * snapshot-uri de valori, nu referințe către food_items.
     */
    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        val dao = db.foodItemDao()
        val prefs = context.getSharedPreferences("food_seed_prefs", Context.MODE_PRIVATE)
        val seededVersion = prefs.getInt("seed_version", 0)
        if (seededVersion >= SEED_VERSION) return@withContext
        dao.deleteAll()
        dao.insertAll(FOOD_ITEMS)
        prefs.edit().putInt("seed_version", SEED_VERSION).apply()
    }

    companion object {
        private const val SEED_VERSION = 3

        /**
         * Normalizează un text pentru căutare: lowercase + elimină diacriticele
         * latine (ă→a, ș→s, ț→t, é→e, ü→u etc.) păstrând intacte alte alfabete
         * (chirilica pentru ru/uk). Folosit atât pentru a popula `searchKey` în
         * seed, cât și pentru interogarea din ViewModel, ca 'rosii' să găsească 'Roșii'.
         */
        fun normalizeForSearch(vararg parts: String): String {
            val combined = parts.joinToString(" ")
            val decomposed = java.text.Normalizer.normalize(combined.lowercase(Locale.ROOT), java.text.Normalizer.Form.NFD)
            val sb = StringBuilder(decomposed.length)
            for (ch in decomposed) {
                // Sare peste semnele combinatorii (diacriticele) din decompoziția NFD
                if (Character.getType(ch) == Character.NON_SPACING_MARK.toInt()) continue
                sb.append(ch)
            }
            // Litere pe care NFD nu le decompune în ASCII
            return sb.toString()
                .replace("ı", "i")
                .replace("ß", "ss")
                .replace("ł", "l")
                .replace("ø", "o")
                .replace("đ", "d")
                .replace("ð", "d")
                .replace("œ", "oe")
                .replace("æ", "ae")
        }

        /** Numele alimentului în toate limbile aplicației. */
        private data class Names(
            val ro: String, val en: String, val ru: String, val uk: String,
            val fr: String, val de: String, val es: String, val it: String,
            val tr: String, val pt: String, val pl: String
        )

        private fun g(
            n: Names,
            cal: Double,
            protein: Double,
            carbs: Double,
            fat: Double
        ) = FoodItemEntity(
            name = n.ro,
            nameEn = n.en,
            nameRu = n.ru,
            nameUk = n.uk,
            nameFr = n.fr,
            nameDe = n.de,
            nameEs = n.es,
            nameIt = n.it,
            nameTr = n.tr,
            namePt = n.pt,
            namePl = n.pl,
            caloriesPer100g = cal,
            proteinPer100g = protein,
            carbsPer100g = carbs,
            fatPer100g = fat,
            unitType = FoodUnitType.GRAM,
            gramsPerPiece = null,
            searchKey = normalizeForSearch(n.ro, n.en, n.ru, n.uk, n.fr, n.de, n.es, n.it, n.tr, n.pt, n.pl)
        )

        private fun buc(
            n: Names,
            gramsPerPiece: Double,
            calPer100: Double,
            proteinPer100: Double,
            carbsPer100: Double,
            fatPer100: Double
        ) = FoodItemEntity(
            name = n.ro,
            nameEn = n.en,
            nameRu = n.ru,
            nameUk = n.uk,
            nameFr = n.fr,
            nameDe = n.de,
            nameEs = n.es,
            nameIt = n.it,
            nameTr = n.tr,
            namePt = n.pt,
            namePl = n.pl,
            caloriesPer100g = calPer100,
            proteinPer100g = proteinPer100,
            carbsPer100g = carbsPer100,
            fatPer100g = fatPer100,
            unitType = FoodUnitType.PIECE,
            gramsPerPiece = gramsPerPiece,
            searchKey = normalizeForSearch(n.ro, n.en, n.ru, n.uk, n.fr, n.de, n.es, n.it, n.tr, n.pt, n.pl)
        )

        val FOOD_ITEMS: List<FoodItemEntity> = listOf(
            // ===== Carne / Pasăre (per 100g) =====
            g(Names("Piept de pui", "Chicken breast", "Куриная грудка", "Куряча грудка", "Poitrine de poulet", "Hähnchenbrust", "Pechuga de pollo", "Petto di pollo", "Tavuk göğsü", "Peito de frango", "Pierś z kurczaka"), 165.0, 31.0, 0.0, 3.6),
            g(Names("Piept de curcan", "Turkey breast", "Индюшиная грудка", "Індича грудка", "Poitrine de dinde", "Putenbrust", "Pechuga de pavo", "Petto di tacchino", "Hindi göğsü", "Peito de peru", "Pierś z indyka"), 135.0, 21.0, 0.0, 5.0),
            g(Names("Pulpă de pui fără piele", "Skinless chicken thigh", "Куриное бедро без кожи", "Куряче стегно без шкіри", "Cuisse de poulet sans peau", "Hähnchenschenkel ohne Haut", "Muslo de pollo sin piel", "Coscia di pollo senza pelle", "Derisiz tavuk but", "Coxa de frango sem pele", "Udo z kurczaka bez skóry"), 175.0, 25.0, 0.0, 8.0),
            g(Names("Carne de vită slabă", "Lean beef", "Постная говядина", "Пісна яловичина", "Bœuf maigre", "Mageres Rindfleisch", "Carne de res magra", "Manzo magro", "Yağsız sığır eti", "Carne bovina magra", "Chuda wołowina"), 250.0, 26.0, 0.0, 15.0),
            g(Names("Carne de porc (muschi)", "Pork tenderloin", "Свиная вырезка", "Свиняча вирізка", "Filet de porc", "Schweinefilet", "Lomo de cerdo", "Filetto di maiale", "Domuz filetosu", "Lombo de porco", "Polędwiczka wieprzowa"), 143.0, 27.0, 0.0, 3.5),
            g(Names("Carne de vită (tocată, crudă)", "Beef (ground, raw)", "Говядина (фарш, сырая)", "Яловичина (фарш, сира)", "Bœuf (haché, cru)", "Rindfleisch (Hackfleisch, roh)", "Carne de res (molida, cruda)", "Manzo (macinato, crudo)", "Dana eti (kıyma, çiğ)", "Carne bovina (moída, crua)", "Wołowina (mielona, surowa)"), 239.0, 27.3, 0.0, 13.6),
            g(Names("Pulpă de pui (gătită)", "Chicken thigh (cooked)", "Куриное бедро (приготовленное)", "Куряче стегно (приготовлене)", "Cuisse de poulet (cuite)", "Hähnchenkeule (gekocht)", "Muslo de pollo (cocido)", "Coscia di pollo (cotta)", "Tavuk butu (pişmiş)", "Coxa de frango (cozida)", "Udko kurczaka (gotowane)"), 209.0, 20.0, 0.0, 14.0),
            g(Names("Cotlet de porc (gătit)", "Pork chop (cooked)", "Свиная отбивная (приготовленная)", "Свиняча відбивна (приготовлена)", "Côtelette de porc (cuite)", "Schweinekotelett (gekocht)", "Chuleta de cerdo (cocida)", "Costoletta di maiale (cotta)", "Domuz pirzolası (pişmiş)", "Costeleta de porco (cozida)", "Kotlet schabowy (gotowany)"), 242.0, 27.0, 0.0, 14.0),
            g(Names("Bacon (slănină afumată)", "Bacon", "Бекон", "Бекон", "Bacon", "Speck", "Tocino (bacon)", "Pancetta (bacon)", "Pastırma (bacon)", "Bacon", "Boczek (bekon)"), 337.0, 14.0, 1.3, 30.0),
            g(Names("Miel (pulpă, gătit)", "Lamb (leg, cooked)", "Баранина (нога, приготовленная)", "Баранина (нога, приготовлена)", "Agneau (gigot, cuit)", "Lamm (Keule, gekocht)", "Cordero (pierna, cocido)", "Agnello (coscia, cotto)", "Kuzu eti (but, pişmiş)", "Cordeiro (perna, cozido)", "Jagnięcina (udziec, gotowana)"), 187.0, 20.0, 0.0, 11.0),
            g(Names("Piept de rață (fără piele, gătit)", "Duck breast (skinless, cooked)", "Утиная грудка (без кожи, приготовленная)", "Качина грудка (без шкіри, приготовлена)", "Magret de canard (sans peau, cuit)", "Entenbrust (ohne Haut, gekocht)", "Pechuga de pato (sin piel, cocida)", "Petto d'anatra (senza pelle, cotto)", "Ördek göğsü (derisiz, pişmiş)", "Peito de pato (sem pele, cozido)", "Pierś kaczki (bez skóry, gotowana)"), 133.0, 24.0, 0.0, 4.0),
            g(Names("Cârnați (de porc)", "Sausage (pork)", "Колбаса (свиная)", "Ковбаса (свиняча)", "Saucisse (de porc)", "Wurst (Schwein)", "Salchicha (de cerdo)", "Salsiccia (di maiale)", "Sosis (domuz)", "Linguiça (de porco)", "Kiełbasa (wieprzowa)"), 301.0, 12.0, 2.0, 27.0),

            // ===== Pește / Fructe de mare (per 100g) =====
            g(Names("Somon", "Salmon", "Лосось", "Лосось", "Saumon", "Lachs", "Salmón", "Salmone", "Somon", "Salmão", "Łosoś"), 208.0, 20.0, 0.0, 13.0),
            g(Names("Ton în conservă", "Canned tuna", "Консервированный тунец", "Консервований тунець", "Thon en conserve", "Thunfisch in der Dose", "Atún en lata", "Tonno in scatola", "Konserve ton balığı", "Atum em lata", "Tuńczyk w puszce"), 116.0, 26.0, 0.0, 1.0),
            g(Names("Cod", "Cod", "Треска", "Тріска", "Cabillaud", "Kabeljau", "Bacalao", "Merluzzo", "Morina", "Bacalhau", "Dorsz"), 82.0, 18.0, 0.0, 0.7),
            g(Names("Creveți", "Shrimp", "Креветки", "Креветки", "Crevettes", "Garnelen", "Gambas", "Gamberi", "Karides", "Camarões", "Krewetki"), 99.0, 24.0, 0.2, 0.3),
            g(Names("Sardine (conservate în ulei)", "Sardines (canned in oil)", "Сардины (консервированные в масле)", "Сардини (консервовані в олії)", "Sardines (en conserve à l'huile)", "Sardinen (in Öl, Dose)", "Sardinas (en lata con aceite)", "Sardine (in scatola sott'olio)", "Sardalya (yağlı konserve)", "Sardinhas (em conserva no azeite)", "Sardynki (w puszce, w oleju)"), 208.0, 25.0, 0.0, 11.5),

            // ===== Lactate / Ouă (per 100g) =====
            g(Names("Iaurt grecesc", "Greek yogurt", "Греческий йогурт", "Грецький йогурт", "Yaourt grec", "Griechischer Joghurt", "Yogur griego", "Yogurt greco", "Yunan yoğurdu", "Iogurte grego", "Jogurt grecki"), 97.0, 9.0, 4.0, 5.0),
            g(Names("Iaurt natural", "Plain yogurt", "Натуральный йогурт", "Натуральний йогурт", "Yaourt nature", "Naturjoghurt", "Yogur natural", "Yogurt naturale", "Sade yoğurt", "Iogurte natural", "Jogurt naturalny"), 61.0, 3.5, 4.7, 3.3),
            g(Names("Brânză de vaci", "Cottage cheese", "Творог", "Творог", "Fromage blanc", "Hüttenkäse", "Requesón", "Ricotta", "Süzme peynir", "Queijo cottage", "Twaróg"), 98.0, 11.0, 3.4, 4.3),
            g(Names("Telemea", "Feta cheese", "Брынза", "Бринза", "Feta", "Schafskäse", "Queso feta", "Feta", "Beyaz peynir", "Queijo feta", "Ser feta"), 265.0, 18.0, 2.0, 21.0),
            g(Names("Mozzarella", "Mozzarella", "Моцарелла", "Моцарела", "Mozzarella", "Mozzarella", "Mozzarella", "Mozzarella", "Mozzarella", "Mozzarella", "Mozzarella"), 280.0, 22.0, 2.0, 22.0),
            g(Names("Lapte 1.5%", "Milk 1.5%", "Молоко 1,5%", "Молоко 1,5%", "Lait 1,5%", "Milch 1,5%", "Leche 1,5%", "Latte 1,5%", "%1,5 süt", "Leite 1,5%", "Mleko 1,5%"), 44.0, 3.4, 4.8, 1.5),
            g(Names("Lapte integral", "Whole milk", "Цельное молоко", "Цільне молоко", "Lait entier", "Vollmilch", "Leche entera", "Latte intero", "Tam yağlı süt", "Leite integral", "Pełne mleko"), 61.0, 3.2, 4.8, 3.3),
            g(Names("Chefir", "Kefir", "Кефир", "Кефір", "Kéfir", "Kefir", "Kéfir", "Kefir", "Kefir", "Kefir", "Kefir"), 51.0, 3.4, 4.0, 2.6),
            g(Names("Lapte (1% grăsime)", "Milk (1% fat)", "Молоко (1% жирности)", "Молоко (1% жирності)", "Lait (1% de matière grasse)", "Milch (1% Fett)", "Leche (1% grasa)", "Latte (1% grassi)", "Süt (%1 yağlı)", "Leite (1% gordura)", "Mleko (1% tłuszczu)"), 42.0, 3.4, 5.0, 1.0),
            g(Names("Iaurt grecesc (0% grăsime)", "Greek yogurt (0% fat)", "Греческий йогурт (0% жирности)", "Грецький йогурт (0% жирності)", "Yaourt grec (0% MG)", "Griechischer Joghurt (0% Fett)", "Yogur griego (0% grasa)", "Yogurt greco (0% grassi)", "Yunan yoğurdu (%0 yağ)", "Iogurte grego (0% gordura)", "Jogurt grecki (0% tłuszczu)"), 59.0, 10.0, 3.6, 0.4),
            g(Names("Brânză cheddar", "Cheddar cheese", "Сыр чеддер", "Сир чеддер", "Fromage cheddar", "Cheddar-Käse", "Queso cheddar", "Formaggio cheddar", "Çedar peyniri", "Queijo cheddar", "Ser cheddar"), 402.0, 25.0, 1.3, 33.0),
            g(Names("Unt", "Butter", "Масло сливочное", "Масло вершкове", "Beurre", "Butter", "Mantequilla", "Burro", "Tereyağı", "Manteiga", "Masło"), 717.0, 0.9, 0.1, 81.0),
            g(Names("Smântână (20%)", "Sour cream (20%)", "Сметана (20%)", "Сметана (20%)", "Crème fraîche (20%)", "Sauerrahm (20%)", "Crema agria (20%)", "Panna acida (20%)", "Ekşi krema (%20)", "Creme azedo (20%)", "Śmietana (20%)"), 196.0, 2.7, 3.4, 19.0),
            g(Names("Brânză de vaci (0% grăsime)", "Cottage cheese (0% fat)", "Творог (обезжиренный)", "Сир кисломолочний (знежирений)", "Fromage cottage (0% MG)", "Hüttenkäse (0% Fett)", "Requesón (0% grasa)", "Fiocchi di latte (0% grassi)", "Süzme peynir (%0 yağ)", "Queijo cottage (0% gordura)", "Twaróg (0% tłuszczu)"), 72.0, 12.4, 0.7, 2.0),

            // ===== Legume (per 100g) =====
            g(Names("Broccoli", "Broccoli", "Брокколи", "Броколі", "Brocoli", "Brokkoli", "Brócoli", "Broccoli", "Brokoli", "Brócolis", "Brokuły"), 34.0, 2.8, 7.0, 0.4),
            g(Names("Spanac", "Spinach", "Шпинат", "Шпинат", "Épinards", "Spinat", "Espinacas", "Spinaci", "Ispanak", "Espinafre", "Szpinak"), 23.0, 2.9, 3.6, 0.4),
            g(Names("Morcov", "Carrot", "Морковь", "Морква", "Carotte", "Karotte", "Zanahoria", "Carota", "Havuç", "Cenoura", "Marchewka"), 41.0, 0.9, 10.0, 0.2),
            g(Names("Roșii", "Tomatoes", "Помидоры", "Помідори", "Tomates", "Tomaten", "Tomates", "Pomodori", "Domates", "Tomates", "Pomidory"), 18.0, 0.9, 3.9, 0.2),
            g(Names("Castravete", "Cucumber", "Огурец", "Огірок", "Concombre", "Gurke", "Pepino", "Cetriolo", "Salatalık", "Pepino", "Ogórek"), 15.0, 0.7, 3.6, 0.1),
            g(Names("Ardei gras", "Bell pepper", "Болгарский перец", "Болгарський перець", "Poivron", "Paprika", "Pimiento", "Peperone", "Dolmalık biber", "Pimentão", "Papryka"), 31.0, 1.0, 6.0, 0.3),
            g(Names("Dovlecel", "Zucchini", "Кабачок", "Кабачок", "Courgette", "Zucchini", "Calabacín", "Zucchina", "Kabak", "Abobrinha", "Cukinia"), 17.0, 1.2, 3.1, 0.3),
            g(Names("Conopidă", "Cauliflower", "Цветная капуста", "Цвітна капуста", "Chou-fleur", "Blumenkohl", "Coliflor", "Cavolfiore", "Karnabahar", "Couve-flor", "Kalafior"), 25.0, 1.9, 5.0, 0.3),
            g(Names("Ciuperci", "Mushrooms", "Грибы", "Гриби", "Champignons", "Pilze", "Champiñones", "Funghi", "Mantar", "Cogumelos", "Grzyby"), 22.0, 3.1, 3.3, 0.3),
            g(Names("Avocado", "Avocado", "Авокадо", "Авокадо", "Avocat", "Avocado", "Aguacate", "Avocado", "Avokado", "Abacate", "Awokado"), 160.0, 2.0, 9.0, 15.0),
            g(Names("Ceapă", "Onion", "Лук репчатый", "Цибуля ріпчаста", "Oignon", "Zwiebel", "Cebolla", "Cipolla", "Soğan", "Cebola", "Cebula"), 40.0, 1.1, 9.3, 0.1),
            g(Names("Usturoi", "Garlic", "Чеснок", "Часник", "Ail", "Knoblauch", "Ajo", "Aglio", "Sarımsak", "Alho", "Czosnek"), 149.0, 6.4, 33.1, 0.5),
            g(Names("Varză (albă)", "Cabbage (white)", "Капуста (белокочанная)", "Капуста (білокачанна)", "Chou (blanc)", "Kohl (weiß)", "Col (blanca)", "Cavolo (bianco)", "Lahana (beyaz)", "Repolho (branco)", "Kapusta (biała)"), 25.0, 1.8, 6.0, 0.1),
            g(Names("Sfeclă roșie", "Beetroot", "Свёкла", "Буряк", "Betterave", "Rote Bete", "Remolacha", "Barbabietola", "Pancar", "Beterraba", "Burak"), 43.0, 1.6, 9.6, 0.2),
            g(Names("Vânătă", "Eggplant (aubergine)", "Баклажан", "Баклажан", "Aubergine", "Aubergine", "Berenjena", "Melanzana", "Patlıcan", "Berinjela", "Bakłażan"), 25.0, 1.0, 6.0, 0.2),
            g(Names("Salată verde (iceberg)", "Lettuce (iceberg)", "Салат (айсберг)", "Салат (айсберг)", "Laitue (iceberg)", "Kopfsalat (Eisberg)", "Lechuga (iceberg)", "Lattuga (iceberg)", "Marul (buzul)", "Alface (iceberg)", "Sałata (lodowa)"), 15.0, 0.6, 3.6, 0.2),
            g(Names("Porumb (dulce, fiert)", "Corn (sweet, cooked)", "Кукуруза (сладкая, варёная)", "Кукурудза (солодка, варена)", "Maïs (doux, cuit)", "Mais (süß, gekocht)", "Maíz (dulce, cocido)", "Mais (dolce, cotto)", "Mısır (tatlı, pişmiş)", "Milho (doce, cozido)", "Kukurydza (słodka, gotowana)"), 86.0, 3.3, 19.0, 1.4),
            g(Names("Mazăre verde", "Green peas", "Зелёный горошек", "Зелений горошок", "Petits pois", "Grüne Erbsen", "Guisantes verdes", "Piselli verdi", "Bezelye", "Ervilhas verdes", "Groszek zielony"), 42.0, 2.6, 7.6, 0.4),

            // ===== Fructe (per 100g) =====
            g(Names("Mere", "Apples", "Яблоки", "Яблука", "Pommes", "Äpfel", "Manzanas", "Mele", "Elma", "Maçãs", "Jabłka"), 52.0, 0.3, 14.0, 0.2),
            g(Names("Struguri", "Grapes", "Виноград", "Виноград", "Raisins", "Trauben", "Uvas", "Uva", "Üzüm", "Uvas", "Winogrona"), 69.0, 0.7, 18.0, 0.2),
            g(Names("Căpșuni", "Strawberries", "Клубника", "Полуниця", "Fraises", "Erdbeeren", "Fresas", "Fragole", "Çilek", "Morangos", "Truskawki"), 32.0, 0.7, 7.7, 0.3),
            g(Names("Afine", "Blueberries", "Черника", "Чорниця", "Myrtilles", "Blaubeeren", "Arándanos", "Mirtilli", "Yaban mersini", "Mirtilos", "Jagody"), 57.0, 0.7, 14.0, 0.3),
            g(Names("Zmeură", "Raspberries", "Малина", "Малина", "Framboises", "Himbeeren", "Frambuesas", "Lamponi", "Ahududu", "Framboesas", "Maliny"), 52.0, 1.2, 12.0, 0.7),
            g(Names("Ananas", "Pineapple", "Ананас", "Ананас", "Ananas", "Ananas", "Piña", "Ananas", "Ananas", "Abacaxi", "Ananas"), 50.0, 0.5, 13.0, 0.1),
            g(Names("Pepene", "Watermelon", "Арбуз", "Кавун", "Pastèque", "Wassermelone", "Sandía", "Anguria", "Karpuz", "Melancia", "Arbuz"), 30.0, 0.6, 8.0, 0.2),
            g(Names("Mango", "Mango", "Манго", "Манго", "Mangue", "Mango", "Mango", "Mango", "Mango", "Manga", "Mango"), 60.0, 0.8, 15.0, 0.4),

            // ===== Cereale (per 100g) =====
            g(Names("Orez alb fiert", "Cooked white rice", "Варёный белый рис", "Варений білий рис", "Riz blanc cuit", "Gekochter weißer Reis", "Arroz blanco cocido", "Riso bianco cotto", "Pişmiş beyaz pirinç", "Arroz branco cozido", "Ugotowany biały ryż"), 130.0, 2.7, 28.0, 0.3),
            g(Names("Orez brun fiert", "Cooked brown rice", "Варёный коричневый рис", "Варений бурий рис", "Riz brun cuit", "Gekochter brauner Reis", "Arroz integral cocido", "Riso integrale cotto", "Pişmiş esmer pirinç", "Arroz integral cozido", "Ugotowany brązowy ryż"), 112.0, 2.6, 24.0, 0.9),
            g(Names("Paste fierte", "Cooked pasta", "Варёные макароны", "Варені макарони", "Pâtes cuites", "Gekochte Nudeln", "Pasta cocida", "Pasta cotta", "Pişmiş makarna", "Massa cozida", "Ugotowany makaron"), 131.0, 5.0, 25.0, 1.1),
            g(Names("Cartofi fierți", "Boiled potatoes", "Варёный картофель", "Варена картопля", "Pommes de terre bouillies", "Gekochte Kartoffeln", "Patatas hervidas", "Patate bollite", "Haşlanmış patates", "Batatas cozidas", "Ziemniaki gotowane"), 87.0, 2.0, 20.0, 0.1),
            g(Names("Cartof dulce", "Sweet potato", "Сладкий картофель", "Солодка картопля", "Patate douce", "Süßkartoffel", "Boniato", "Patata dolce", "Tatlı patates", "Batata-doce", "Słodki ziemniak"), 86.0, 1.6, 20.0, 0.1),
            g(Names("Fulgi de ovăz", "Oat flakes", "Овсяные хлопья", "Вівсяні пластівці", "Flocons d'avoine", "Haferflocken", "Copos de avena", "Fiocchi d'avena", "Yulaf ezmesi", "Flocos de aveia", "Płatki owsiane"), 389.0, 17.0, 66.0, 7.0),
            g(Names("Quinoa fiartă", "Cooked quinoa", "Варёная киноа", "Варена кіноа", "Quinoa cuite", "Gekochte Quinoa", "Quinoa cocida", "Quinoa cotta", "Pişmiş kinoa", "Quinoa cozida", "Ugotowana komosa ryżowa"), 120.0, 4.4, 21.0, 1.9),
            g(Names("Fasole neagră fiartă", "Cooked black beans", "Варёная чёрная фасоль", "Варена чорна квасоля", "Haricots noirs cuits", "Gekochte schwarze Bohnen", "Frijoles negros cocidos", "Fagioli neri cotti", "Pişmiş siyah fasulye", "Feijão preto cozido", "Ugotowana czarna fasola"), 132.0, 9.0, 24.0, 0.5),
            g(Names("Năut fiert", "Cooked chickpeas", "Варёный нут", "Варений нут", "Pois chiches cuits", "Gekochte Kichererbsen", "Garbanzos cocidos", "Ceci cotti", "Pişmiş nohut", "Grão-de-bico cozido", "Ugotowana ciecierzyca"), 164.0, 9.0, 27.0, 2.6),
            g(Names("Linte fiartă", "Cooked lentils", "Варёная чечевица", "Варена сочевиця", "Lentilles cuites", "Gekochte Linsen", "Lentejas cocidas", "Lenticchie cotte", "Pişmiş mercimek", "Lentilhas cozidas", "Ugotowana soczewica"), 116.0, 9.0, 20.0, 0.4),
            g(Names("Couscous fiert", "Cooked couscous", "Варёный кускус", "Варений кускус", "Couscous cuit", "Gekochter Couscous", "Cuscús cocido", "Couscous cotto", "Pişmiş kuskus", "Cuscuz cozido", "Ugotowany kuskus"), 112.0, 3.8, 23.0, 0.2),
            g(Names("Paste (integrale, fierte)", "Pasta (whole wheat, cooked)", "Макароны (цельнозерновые, варёные)", "Макарони (цільнозернові, варені)", "Pâtes (complètes, cuites)", "Nudeln (Vollkorn, gekocht)", "Pasta (integral, cocida)", "Pasta (integrale, cotta)", "Makarna (tam buğday, pişmiş)", "Massa (integral, cozida)", "Makaron (pełnoziarnisty, gotowany)"), 124.0, 5.0, 25.0, 0.6),
            g(Names("Terci de ovăz (fiert)", "Oatmeal (cooked)", "Овсяная каша (варёная)", "Вівсяна каша (варена)", "Porridge (cuit)", "Haferbrei (gekocht)", "Avena cocida", "Porridge di avena (cotto)", "Yulaf lapası (pişmiş)", "Aveia cozida", "Owsianka (gotowana)"), 68.0, 2.5, 12.0, 1.0),
            g(Names("Hrișcă (fiartă)", "Buckwheat (cooked)", "Гречка (варёная)", "Гречка (варена)", "Sarrasin (cuit)", "Buchweizen (gekocht)", "Trigo sarraceno (cocido)", "Grano saraceno (cotto)", "Karabuğday (pişmiş)", "Trigo sarraceno (cozido)", "Kasza gryczana (gotowana)"), 92.0, 3.4, 18.0, 0.6),
            g(Names("Fasole roșie (fiartă)", "Kidney beans (cooked)", "Фасоль красная (варёная)", "Квасоля червона (варена)", "Haricots rouges (cuits)", "Kidneybohnen (gekocht)", "Judías rojas (cocidas)", "Fagioli rossi (cotti)", "Kırmızı fasulye (pişmiş)", "Feijão vermelho (cozido)", "Fasola czerwona (gotowana)"), 127.0, 8.7, 22.5, 0.5),
            g(Names("Tofu (tare)", "Tofu (firm)", "Тофу (твёрдый)", "Тофу (твердий)", "Tofu (ferme)", "Tofu (fest)", "Tofu (firme)", "Tofu (compatto)", "Tofu (sert)", "Tofu (firme)", "Tofu (twarde)"), 147.0, 12.9, 11.1, 6.4),

            // ===== Nuci / Semințe (per 100g) =====
            g(Names("Unt de arahide", "Peanut butter", "Арахисовое масло", "Арахісова паста", "Beurre de cacahuète", "Erdnussbutter", "Mantequilla de cacahuete", "Burro di arachidi", "Fıstık ezmesi", "Manteiga de amendoim", "Masło orzechowe"), 588.0, 21.2, 22.0, 50.6),
            g(Names("Migdale", "Almonds", "Миндаль", "Мигдаль", "Amandes", "Mandeln", "Almendras", "Mandorle", "Badem", "Amêndoas", "Migdały"), 579.0, 21.2, 21.6, 49.9),
            g(Names("Nuci", "Walnuts", "Грецкие орехи", "Волоські горіхи", "Noix", "Walnüsse", "Nueces", "Noci", "Ceviz", "Nozes", "Orzechy włoskie"), 654.0, 15.2, 17.0, 60.8),
            g(Names("Semințe de chia", "Chia seeds", "Семена чиа", "Насіння чіа", "Graines de chia", "Chiasamen", "Semillas de chía", "Semi di chia", "Chia tohumu", "Sementes de chia", "Nasiona chia"), 486.0, 18.3, 42.1, 30.7),
            g(Names("Ulei de măsline", "Olive oil", "Оливковое масло", "Оливкова олія", "Huile d'olive", "Olivenöl", "Aceite de oliva", "Olio d'oliva", "Zeytinyağı", "Azeite de oliva", "Oliwa z oliwek"), 884.0, 0.0, 0.0, 100.0),
            g(Names("Miere", "Honey", "Мёд", "Мед", "Miel", "Honig", "Miel", "Miele", "Bal", "Mel", "Miód"), 304.0, 0.3, 82.4, 0.0),
            g(Names("Hummus", "Hummus", "Хумус", "Хумус", "Houmous", "Hummus", "Hummus", "Hummus", "Humus", "Homus", "Hummus"), 166.0, 8.9, 11.0, 9.0),
            g(Names("Arahide", "Peanuts", "Арахис", "Арахіс", "Cacahuètes", "Erdnüsse", "Cacahuetes (maní)", "Arachidi", "Yer fıstığı", "Amendoins", "Orzeszki ziemne"), 567.0, 25.8, 16.1, 49.2),
            g(Names("Caju", "Cashews", "Кешью", "Кеш'ю", "Noix de cajou", "Cashewnüsse", "Anacardos", "Anacardi", "Kaju fıstığı", "Castanha de caju", "Orzechy nerkowca"), 553.0, 18.2, 30.2, 43.9),
            g(Names("Semințe de floarea soarelui", "Sunflower seeds", "Семечки подсолнечника", "Насіння соняшнику", "Graines de tournesol", "Sonnenblumenkerne", "Semillas de girasol", "Semi di girasole", "Ayçiçeği çekirdeği", "Sementes de girassol", "Nasiona słonecznika"), 584.0, 20.8, 3.7, 51.5),
            g(Names("Semințe de dovleac", "Pumpkin seeds", "Тыквенные семечки", "Гарбузове насіння", "Graines de courge", "Kürbiskerne", "Semillas de calabaza", "Semi di zucca", "Kabak çekirdeği", "Sementes de abóbora", "Pestki dyni"), 559.0, 30.2, 9.0, 42.2),
            g(Names("Semințe de in", "Flaxseeds", "Семена льна", "Насіння льону", "Graines de lin", "Leinsamen", "Semillas de lino", "Semi di lino", "Keten tohumu", "Sementes de linhaça", "Siemię lniane"), 534.0, 18.3, 28.9, 42.2),
            g(Names("Alune de pădure", "Hazelnuts", "Фундук", "Фундук", "Noisettes", "Haselnüsse", "Avellanas", "Nocciole", "Fındık", "Avelãs", "Orzechy laskowe"), 628.0, 14.9, 16.7, 60.7),
            g(Names("Fistic", "Pistachios", "Фисташки", "Фісташки", "Pistaches", "Pistazien", "Pistachos", "Pistacchi", "Antep fıstığı", "Pistácios", "Pistacje"), 573.0, 20.2, 27.2, 45.8),
            g(Names("Ulei de floarea soarelui", "Sunflower oil", "Подсолнечное масло", "Соняшникова олія", "Huile de tournesol", "Sonnenblumenöl", "Aceite de girasol", "Olio di girasole", "Ayçiçek yağı", "Óleo de girassol", "Olej słonecznikowy"), 884.0, 0.0, 0.0, 100.0),
            g(Names("Ulei de cocos", "Coconut oil", "Кокосовое масло", "Кокосова олія", "Huile de coco", "Kokosöl", "Aceite de coco", "Olio di cocco", "Hindistan cevizi yağı", "Óleo de coco", "Olej kokosowy"), 862.0, 0.0, 0.0, 99.1),

            // ===== Altele / Panificație / Dulciuri (per 100g) =====
            g(Names("Ciocolată neagră (70%)", "Dark chocolate (70%)", "Тёмный шоколад (70%)", "Чорний шоколад (70%)", "Chocolat noir (70%)", "Zartbitterschokolade (70%)", "Chocolate negro (70%)", "Cioccolato fondente (70%)", "Bitter çikolata (%70)", "Chocolate amargo (70%)", "Czekolada gorzka (70%)"), 546.0, 4.9, 60.0, 31.3),
            g(Names("Zahăr (alb)", "Sugar (white)", "Сахар (белый)", "Цукор (білий)", "Sucre (blanc)", "Zucker (weiß)", "Azúcar (blanco)", "Zucchero (bianco)", "Şeker (beyaz)", "Açúcar (branco)", "Cukier (biały)"), 387.0, 0.0, 100.0, 0.0),
            g(Names("Făină de grâu (albă)", "Wheat flour (white)", "Пшеничная мука (белая)", "Пшеничне борошно (біле)", "Farine de blé (blanche)", "Weizenmehl (weiß)", "Harina de trigo (blanca)", "Farina di grano (bianca)", "Buğday unu (beyaz)", "Farinha de trigo (branca)", "Mąka pszenna (biała)"), 364.0, 10.3, 76.3, 1.0),
            g(Names("Gem (de căpșuni)", "Jam (strawberry)", "Джем (клубничный)", "Джем (полуничний)", "Confiture (de fraises)", "Marmelade (Erdbeere)", "Mermelada (de fresa)", "Marmellata (di fragole)", "Reçel (çilek)", "Geleia (de morango)", "Dżem (truskawkowy)"), 282.0, 1.5, 74.0, 0.1),
            g(Names("Stafide", "Raisins", "Изюм", "Родзинки", "Raisins secs", "Rosinen", "Pasas", "Uvetta", "Kuru üzüm", "Passas", "Rodzynki"), 299.0, 1.5, 66.0, 0.5),
            g(Names("Curmale (uscate)", "Dates (dried)", "Финики (сушёные)", "Фініки (сушені)", "Dattes (séchées)", "Datteln (getrocknet)", "Dátiles (secos)", "Datteri (secchi)", "Hurma (kuru)", "Tâmaras (secas)", "Daktyle (suszone)"), 277.0, 1.8, 75.0, 0.2),
            g(Names("Cartofi prăjiți (copți)", "French fries (baked)", "Картофель фри (запечённый)", "Картопля фрі (запечена)", "Frites (cuites au four)", "Pommes frites (gebacken)", "Patatas fritas (al horno)", "Patatine fritte (al forno)", "Patates kızartması (fırında)", "Batata frita (assada)", "Frytki (pieczone)"), 143.0, 3.0, 23.7, 3.6),
            g(Names("Cafea (neagră, fără zahăr)", "Coffee (black, no sugar)", "Кофе (чёрный, без сахара)", "Кава (чорна, без цукру)", "Café (noir, sans sucre)", "Kaffee (schwarz, ohne Zucker)", "Café (negro, sin azúcar)", "Caffè (nero, senza zucchero)", "Kahve (sade, şekersiz)", "Café (preto, sem açúcar)", "Kawa (czarna, bez cukru)"), 2.0, 0.3, 0.0, 0.0),
            g(Names("Suc de portocale (proaspăt)", "Orange juice (fresh)", "Апельсиновый сок (свежий)", "Апельсиновий сік (свіжий)", "Jus d'orange (frais)", "Orangensaft (frisch)", "Zumo de naranja (natural)", "Succo d'arancia (fresco)", "Portakal suyu (taze)", "Suco de laranja (fresco)", "Sok pomarańczowy (świeży)"), 45.0, 0.3, 10.4, 0.1),

            // ===== Alimente numărate pe bucăți (PIECE) =====
            buc(Names("Ou", "Egg (chicken)", "Яйцо (куриное)", "Яйце (куряче)", "Œuf (de poule)", "Ei (Huhn)", "Huevo (de gallina)", "Uovo (di gallina)", "Yumurta (tavuk)", "Ovo (de galinha)", "Jajko (kurze)"), 60.0, 155.0, 12.6, 1.1, 10.6),
            buc(Names("Banană", "Banana", "Банан", "Банан", "Banane", "Banane", "Plátano (banana)", "Banana", "Muz", "Banana", "Banan"), 118.0, 89.0, 1.1, 22.8, 0.3),
            buc(Names("Măr", "Apple", "Яблоко", "Яблуко", "Pomme", "Apfel", "Manzana", "Mela", "Elma", "Maçã", "Jabłko"), 182.0, 52.0, 0.3, 14.0, 0.2),
            buc(Names("Portocală", "Orange", "Апельсин", "Апельсин", "Orange", "Orange", "Naranja", "Arancia", "Portakal", "Laranja", "Pomarańcza"), 131.0, 47.0, 0.9, 12.0, 0.1),
            buc(Names("Pâine (felie)", "Bread (slice)", "Хлеб (ломтик)", "Хліб (скибка)", "Pain (tranche)", "Brot (Scheibe)", "Pan (rebanada)", "Pane (fetta)", "Ekmek (dilim)", "Pão (fatia)", "Chleb (kromka)"), 30.0, 265.0, 9.4, 49.0, 3.2),
            buc(Names("Pâine integrală (felie)", "Whole wheat bread (slice)", "Цельнозерновой хлеб (ломтик)", "Цільнозерновий хліб (скибка)", "Pain complet (tranche)", "Vollkornbrot (Scheibe)", "Pan integral (rebanada)", "Pane integrale (fetta)", "Tam buğday ekmeği (dilim)", "Pão integral (fatia)", "Chleb pełnoziarnisty (kromka)"), 32.0, 247.0, 13.0, 41.3, 3.4),
            buc(Names("Avocado (jumătate)", "Avocado (half)", "Авокадо (половина)", "Авокадо (половина)", "Avocat (moitié)", "Avocado (halbe)", "Aguacate (medio)", "Avocado (metà)", "Avokado (yarım)", "Abacate (metade)", "Awokado (połówka)"), 75.0, 160.0, 2.0, 8.5, 14.7),
            buc(Names("Baton proteic", "Protein bar", "Протеиновый батончик", "Протеїновий батончик", "Barre protéinée", "Proteinriegel", "Barrita proteica", "Barretta proteica", "Protein bar", "Barra de proteína", "Baton białkowy"), 60.0, 350.0, 30.0, 35.0, 10.0),
            buc(Names("Iaurt de băut", "Drinkable yogurt", "Питьевой йогурт", "Питний йогурт", "Yaourt à boire", "Trinkjoghurt", "Yogur para beber", "Yogurt da bere", "İçilebilir yoğurt", "Iogurte líquido", "Jogurt pitny"), 200.0, 65.0, 3.0, 9.0, 1.5),
            buc(Names("Piersică", "Peach", "Персик", "Персик", "Pêche", "Pfirsich", "Melocotón", "Pesca", "Şeftali", "Pêssego", "Brzoskwinia"), 150.0, 39.0, 1.1, 9.1, 0.3),
            buc(Names("Pară", "Pear", "Груша", "Груша", "Poire", "Birne", "Pera", "Pera", "Armut", "Pera", "Gruszka"), 178.0, 57.0, 0.4, 15.2, 0.1),
            buc(Names("Kiwi", "Kiwi", "Киви", "Ківі", "Kiwi", "Kiwi", "Kiwi", "Kiwi", "Kivi", "Kiwi", "Kiwi"), 76.0, 61.0, 1.1, 14.7, 0.5),
            buc(Names("Lămâie", "Lemon", "Лимон", "Лимон", "Citron", "Zitrone", "Limón", "Limone", "Limon", "Limão", "Cytryna"), 58.0, 30.0, 0.7, 8.0, 0.2),
            buc(Names("Croissant", "Croissant", "Круассан", "Круасан", "Croissant", "Croissant", "Cruasán", "Croissant (cornetto)", "Kruvasan", "Croissant", "Rogalik (croissant)"), 60.0, 406.0, 4.4, 49.0, 21.0)
        )
    }
}
