package pl.ing.housingmarket.model;

import lombok.Getter;

@Getter
public enum Region {
    DLN_WROC_C("Dolnośląskie - Wrocław centrum"),
    DLN_WROC_PC("Dolnośląskie - Wrocław poza centrum"),
    DLN_POZA_WROC("Dolnośląskie - poza Wrocławiem"),
    SL_POL("Śląskie - południe"),
    SL_KATO("Śląskie - Katowice"),
    SL_PN("Śląskie - północ"),
    M_WAW_CE("Mazowieckie - Warszawa Centrum"),
    M_WAW_W("Mazowieckie - Warszawa wschód"),
    M_WAW_Z("Mazowieckie - Warszawa zachód"),
    LUBL("Lubelskie - Lublin"),
    LUBL_INNE("Lubelskie - poza Lublinem"),
    ZPOM("Zachodniopomorskie"),
    LUBSK("Lubuskie");

    private final String description;

    Region(String description) {
        this.description = description;
    }

}
