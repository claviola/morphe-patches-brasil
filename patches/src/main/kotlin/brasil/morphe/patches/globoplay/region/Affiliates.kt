package brasil.morphe.patches.globoplay.region

/**
 * Static Globo regional affiliate list, generated from docs/globoplay/affiliates.json.
 * The coordinates are representative points known to resolve to each affiliate, not coverage boundaries.
 */
internal data class GloboAffiliate(
    val code: String,
    val name: String,
    val state: String,
    val group: String,
    val latitude: Double,
    val longitude: Double,
)

internal const val CUSTOM_REGION_VALUE = "custom"

internal val GLOBO_AFFILIATES = listOf(
    GloboAffiliate("ACR", "REDE AMAZONICA RIO BRANCO", "AC", "REDE AMAZONICA", -9.9754, -67.8249),
    GloboAffiliate("CZS", "REDE AMAZONICA CRUZEIRO DO SUL", "AC", "REDE AMAZONICA", -7.63, -72.67),
    GloboAffiliate("MAC", "TV ASA BRANCA ALAGOAS", "AL", "TV ASA BRANCA", -9.6658, -35.7353),
    GloboAffiliate("COI", "REDE AMAZONICA COARI", "AM", "REDE AMAZONICA", -3.354, -64.711),
    GloboAffiliate("HUM", "REDE AMAZONICA HUMAITA", "AM", "REDE AMAZONICA", -7.51, -63.02),
    GloboAffiliate("ITC", "REDE AMAZONICA ITACOATIARA", "AM", "REDE AMAZONICA", -3.143, -58.444),
    GloboAffiliate("MAN", "REDE AMAZONICA MANAUS", "AM", "REDE AMAZONICA", -3.119, -60.0217),
    GloboAffiliate("PIN", "REDE AMAZONICA PARINTINS", "AM", "REDE AMAZONICA", -2.628, -56.735),
    GloboAffiliate("AMP", "REDE AMAZONICA MACAPA", "AP", "REDE AMAZONICA", 0.0349, -51.0694),
    GloboAffiliate("BAR", "TV OESTE", "BA", "REDE BAHIA", -12.152, -45.0),
    GloboAffiliate("FEI", "TV SUBAE", "BA", "REDE BAHIA", -12.266, -38.966),
    GloboAffiliate("ITA", "TV SANTA CRUZ", "BA", "REDE BAHIA", -14.785, -39.28),
    GloboAffiliate("JUA", "TV SAO FRANCISCO", "BA", "REDE BAHIA", -9.411, -40.503),
    GloboAffiliate("SAL", "TV BAHIA", "BA", "REDE BAHIA", -12.9777, -38.5016),
    GloboAffiliate("VDC", "TV SUDOESTE", "BA", "REDE BAHIA", -14.861, -40.844),
    GloboAffiliate("CE1", "TV VERDES MARES", "CE", "TV VERDES MARES", -3.7319, -38.5267),
    GloboAffiliate("CE2", "TV VERDES MARES CARIRI", "CE", "TV VERDES MARES", -7.213, -39.315),
    GloboAffiliate("DF", "GLOBO BRASILIA", "DF", "REDE GLOBO", -15.7939, -47.8828),
    GloboAffiliate("CAC", "TV GAZETA SUL", "ES", "REDE GAZETA", -20.848, -41.112),
    GloboAffiliate("ES2", "TV GAZETA NORTE", "ES", "REDE GAZETA", -19.391, -40.072),
    GloboAffiliate("VIT", "TV GAZETA", "ES", "REDE GAZETA", -20.3155, -40.3128),
    GloboAffiliate("GO1", "TV ANHANGUERA GOIANIA\r\nTV ANHANGUERA", "GO", "REDE ANHANGUERA", -16.6869, -49.2648),
    GloboAffiliate("GO2", "TV ANHANGUERA ANAPOLIS", "GO", "REDE ANHANGUERA", -16.328, -48.953),
    GloboAffiliate("IBI", "TV ANHANGUERA ITUMBIARA", "GO", "REDE ANHANGUERA", -18.419, -49.215),
    GloboAffiliate("JAT", "TV ANHANGUERA JATAI", "GO", "REDE ANHANGUERA", -17.879, -51.714),
    GloboAffiliate("LZA", "TV ANHANGUERA LUZIANIA", "GO", "REDE ANHANGUERA", -16.2525, -47.9503),
    GloboAffiliate("PRT", "TV ANHANGUERA PORANGATU", "GO", "REDE ANHANGUERA", -13.44, -49.15),
    GloboAffiliate("RVD", "TV ANHANGUERA RIO VERDE", "GO", "REDE ANHANGUERA", -17.798, -50.928),
    GloboAffiliate("BAS", "TV MIRANTE BALSAS", "MA", "REDE MIRANTE", -7.532, -46.035),
    GloboAffiliate("CDO", "TV MIRANTE COCAIS", "MA", "REDE MIRANTE", -4.858, -43.356),
    GloboAffiliate("IMP", "TV MIRANTE IMPERATRIZ", "MA", "REDE MIRANTE", -5.526, -47.491),
    GloboAffiliate("MA1", "TV MIRANTE SAO LUIS", "MA", "REDE MIRANTE", -2.5307, -44.3068),
    GloboAffiliate("AXA", "TV INTEGRACAO ARAXA", "MG", "TV INTEGRACAO", -19.5934, -46.9403),
    GloboAffiliate("BH", "GLOBO MINAS", "MG", "REDE GLOBO", -19.9167, -43.9345),
    GloboAffiliate("FAB", "INTER TV DOS VALES", "MG", "REDE INTER TV", -18.851, -41.955),
    GloboAffiliate("ITU", "TV INTEGRACAO ITUIUTABA", "MG", "TV INTEGRACAO", -18.969, -49.463),
    GloboAffiliate("JF", "TV INTEGRACAO JUIZ DE FORA", "MG", "TV INTEGRACAO", -21.764, -43.35),
    GloboAffiliate("MTC", "INTER TV GRANDE MINAS", "MG", "REDE INTER TV", -16.735, -43.861),
    GloboAffiliate("UBA", "TV INTEGRACAO UBERABA", "MG", "TV INTEGRACAO", -19.748, -47.932),
    GloboAffiliate("UBE", "TV INTEGRACAO UBERLANDIA", "MG", "TV INTEGRACAO", -18.918, -48.277),
    GloboAffiliate("VAR", "EPTV SUL DE MINAS", "MG", "EPTV", -22.23, -45.936),
    GloboAffiliate("MS1", "TV MORENA CAMPO GRANDE", "MS", "REDE MATOGROSSENSE", -20.4697, -54.6201),
    GloboAffiliate("MS2", "TV MORENA CORUMBA", "MS", "REDE MATOGROSSENSE", -19.009, -57.653),
    GloboAffiliate("POR", "TV MORENA PONTA PORA", "MS", "REDE MATOGROSSENSE", -22.221, -54.806),
    GloboAffiliate("MT", "TV CENTRO AMERICA CUIABA", "MT", "REDE MATOGROSSENSE", -15.601, -56.0974),
    GloboAffiliate("ROD", "TV CENTRO AMERICA RONDONOPOLIS", "MT", "REDE MATOGROSSENSE", -16.47, -54.635),
    GloboAffiliate("SNO", "TV CENTRO AMERICA SINOP", "MT", "REDE MATOGROSSENSE", -11.86, -55.505),
    GloboAffiliate("TGS", "TV CENTRO AMERICA TANGARA DA SERRA", "MT", "REDE MATOGROSSENSE", -14.6229, -57.4933),
    GloboAffiliate("ALT", "TV LIBERAL ALTAMIRA", "PA", "REDE LIBERAL", -3.203, -52.206),
    GloboAffiliate("BEL", "TV LIBERAL BELEM", "PA", "REDE LIBERAL", -1.4558, -48.5039),
    GloboAffiliate("CAS", "TV LIBERAL CASTANHAL", "PA", "REDE LIBERAL", -1.297, -47.922),
    GloboAffiliate("MAB", "TV LIBERAL MARABA", "PA", "REDE LIBERAL", -5.35, -49.117),
    GloboAffiliate("PUP", "TV LIBERAL PARAUAPEBAS", "PA", "REDE LIBERAL", -6.067, -49.902),
    GloboAffiliate("RDC", "TV LIBERAL REDENCAO", "PA", "REDE LIBERAL", -8.028, -50.031),
    GloboAffiliate("TAP", "TV TAPAJOS", "PA", "TV TAPAJOS", -2.443, -54.708),
    GloboAffiliate("CPG", "TV PARAIBA", "PB", "REDE PARAIBA", -7.2306, -35.8811),
    GloboAffiliate("JP", "TV CABO BRANCO", "PB", "REDE PARAIBA", -7.1195, -34.845),
    GloboAffiliate("CRR", "TV ASA BRANCA", "PE", "TV ASA BRANCA", -8.283, -35.976),
    GloboAffiliate("PE1", "GLOBO NORDESTE", "PE", "REDE GLOBO", -8.0476, -34.877),
    GloboAffiliate("PET", "TV GRANDE RIO", "PE", "TV GRANDE RIO", -9.389, -40.503),
    GloboAffiliate("FNO", "TV ALVORADA DO SUL", "PI", "REDE CLUBE", -7.077, -41.467),
    GloboAffiliate("TER", "TV CLUBE", "PI", "REDE CLUBE", -5.0892, -42.8019),
    GloboAffiliate("CAV", "RPC CASCAVEL", "PR", "RPC", -24.955, -53.455),
    GloboAffiliate("CUR", "RPC CURITIBA", "PR", "RPC", -25.4296, -49.2713),
    GloboAffiliate("FOZ", "RPC FOZ DO IGUACU", "PR", "RPC", -25.516, -54.585),
    GloboAffiliate("GAV", "RPC GUARAPUAVA", "PR", "RPC", -25.395, -51.458),
    GloboAffiliate("LON", "RPC LONDRINA", "PR", "RPC", -23.304, -51.169),
    GloboAffiliate("MAR", "RPC MARINGA", "PR", "RPC", -23.42, -51.933),
    GloboAffiliate("PGR", "RPC PONTA GROSSA", "PR", "RPC", -25.095, -50.162),
    GloboAffiliate("PR3", "RPC PARANAVAI", "PR", "RPC", -23.081, -52.465),
    GloboAffiliate("CAB", "INTER TV ALTO LITORAL", "RJ", "REDE INTER TV", -22.889, -42.029),
    GloboAffiliate("FLU", "INTER TV PLANICIE", "RJ", "REDE INTER TV", -21.762, -41.318),
    GloboAffiliate("FRI", "INTER TV SERRA MAR", "RJ", "REDE INTER TV", -22.511, -43.178),
    GloboAffiliate("RES", "TV RIO SUL", "RJ", "TV RIO SUL", -22.52, -44.104),
    GloboAffiliate("RJ", "GLOBO RIO", "RJ", "REDE GLOBO", -22.9068, -43.1729),
    GloboAffiliate("NA2", "INTER TV COSTA BRANCA", "RN", "REDE INTER TV", -5.187, -37.344),
    GloboAffiliate("NAT", "INTER TV CABUGI", "RN", "REDE INTER TV", -5.7945, -35.211),
    GloboAffiliate("ARI", "REDE AMAZONICA ARIQUEMES", "RO", "REDE AMAZONICA", -9.913, -63.04),
    GloboAffiliate("COA", "REDE AMAZONICA CACOAL", "RO", "REDE AMAZONICA", -11.4386, -61.4472),
    GloboAffiliate("JIP", "REDE AMAZONICA JI-PARANA", "RO", "REDE AMAZONICA", -10.878, -61.951),
    GloboAffiliate("RON", "REDE AMAZONICA PORTO VELHO", "RO", "REDE AMAZONICA", -8.7619, -63.9039),
    GloboAffiliate("VLH", "REDE AMAZONICA VILHENA", "RO", "REDE AMAZONICA", -12.7406, -60.1458),
    GloboAffiliate("ROL", "REDE AMAZONICA RORAINOPOLIS", "RR", "REDE AMAZONICA", 1.816, -61.127),
    GloboAffiliate("ROR", "REDE AMAZONICA BOA VISTA", "RR", "REDE AMAZONICA", 2.8235, -60.6758),
    GloboAffiliate("BAG", "RBS TV BAGE", "RS", "RBS", -31.331, -54.106),
    GloboAffiliate("CAL", "RBS TV CRUZ ALTA", "RS", "RBS", -28.645, -53.606),
    GloboAffiliate("CXS", "RBS TV CAXIAS DO SUL", "RS", "RBS", -29.168, -51.179),
    GloboAffiliate("ERE", "RBS TV ERECHIM", "RS", "RBS", -27.634, -52.274),
    GloboAffiliate("PEL", "RBS TV PELOTAS", "RS", "RBS", -31.765, -52.337),
    GloboAffiliate("PFO", "RBS TV PASSO FUNDO", "RS", "RBS", -28.262, -52.409),
    GloboAffiliate("RGE", "RBS TV RIO GRANDE", "RS", "RBS", -32.035, -52.099),
    GloboAffiliate("RS1", "RBS TV PORTO ALEGRE", "RS", "RBS", -30.0346, -51.2177),
    GloboAffiliate("SMA", "RBS TV SANTA MARIA", "RS", "RBS", -29.684, -53.806),
    GloboAffiliate("STC", "RBS TV SANTA CRUZ", "RS", "RBS", -30.0392, -52.8939),
    GloboAffiliate("STR", "RBS TV SANTA ROSA", "RS", "RBS", -27.87, -54.481),
    GloboAffiliate("URU", "RBS TV URUGUAIANA", "RS", "RBS", -29.754, -57.088),
    GloboAffiliate("BLU", "NSC TV BLUMENAU", "SC", "NSC", -26.919, -49.066),
    GloboAffiliate("CHA", "NSC TV CHAPECO", "SC", "NSC", -27.1, -52.615),
    GloboAffiliate("CRI", "NSC TV CRICIUMA", "SC", "NSC", -28.677, -49.369),
    GloboAffiliate("JOA", "NSC TV CENTRO-OESTE", "SC", "NSC", -27.816, -50.326),
    GloboAffiliate("JOI", "NSC TV JOINVILLE", "SC", "NSC", -26.304, -48.846),
    GloboAffiliate("SC1", "NSC TV FLORIANOPOLIS", "SC", "NSC", -27.5954, -48.548),
    GloboAffiliate("SER", "TV SERGIPE", "SE", "TV SERGIPE", -10.9472, -37.0731),
    GloboAffiliate("BAU", "TV TEM BAURU", "SP", "TV TEM", -22.324, -49.087),
    GloboAffiliate("CAM", "EPTV CAMPINAS", "SP", "EPTV", -22.905, -47.061),
    GloboAffiliate("ITP", "TV TEM ITAPETININGA", "SP", "TV TEM", -23.5917, -48.0531),
    GloboAffiliate("RIB", "EPTV RIBEIRAO", "SP", "EPTV", -21.17, -47.81),
    GloboAffiliate("SAN", "TV TRIBUNA", "SP", "TV TRIBUNA", -23.961, -46.332),
    GloboAffiliate("SCA", "EPTV CENTRAL", "SP", "EPTV", -22.0087, -47.8909),
    GloboAffiliate("SJC", "VANGUARDA SAO JOSE DOS CAMPOS", "SP", "REDE VANGUARDA", -23.189, -45.884),
    GloboAffiliate("SJR", "TV TEM SAO JOSE DO RIO PRETO", "SP", "TV TEM", -20.82, -49.379),
    GloboAffiliate("SOR", "TV TEM SOROCABA", "SP", "TV TEM", -23.501, -47.458),
    GloboAffiliate("SP1", "GLOBO SAO PAULO", "SP", "REDE GLOBO", -23.5505, -46.6333),
    GloboAffiliate("TAU", "VANGUARDA TAUBATE", "SP", "REDE VANGUARDA", -23.024, -45.555),
    GloboAffiliate("GUR", "TV ANHANGUERA GURUPI", "TO", "REDE ANHANGUERA", -11.729, -49.068),
    GloboAffiliate("INA", "TV ANHANGUERA ARAGUAINA", "TO", "REDE ANHANGUERA", -7.192, -48.207),
    GloboAffiliate("PAL", "TV ANHANGUERA PALMAS", "TO", "REDE ANHANGUERA", -10.184, -48.3336),
)

private fun String.normalizeWhitespace() = replace(Regex("\\s+"), " ").trim()

/**
 * Dropdown labels to patch-option values. Values are "latitude,longitude", except
 * for the first custom entry. linkedMapOf keeps the dropdown in affiliates.json order.
 */
internal val REGION_OPTION_VALUES: Map<String, String> = linkedMapOf<String, String>().apply {
    GLOBO_AFFILIATES.forEach { affiliate ->
        put(
            "${affiliate.state} — ${affiliate.name.normalizeWhitespace()} (${affiliate.code})",
            "${affiliate.latitude},${affiliate.longitude}",
        )
    }
    // Last entry, so the dropdown opens on real affiliates and a default that works.
    put("Custom coordinates", CUSTOM_REGION_VALUE)
}

/**
 * Default region: São Paulo — GLOBO SAO PAULO (SP1), the network's flagship affiliate.
 * Most users' stock behaviour already resolves to it by IP, so the default changes
 * nothing for them while keeping the patch valid out of the box.
 */
internal const val DEFAULT_REGION_VALUE = "-23.5505,-46.6333"

