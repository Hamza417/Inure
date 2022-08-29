package app.simple.inure.themes.manager

import android.graphics.Color
import app.simple.inure.themes.data.*

enum class Theme(val textViewTheme: TextViewTheme,
                 val viewGroupTheme: ViewGroupTheme,
                 val switchViewTheme: SwitchViewTheme,
                 val iconTheme: IconTheme) {

    LIGHT(
            textViewTheme = TextViewTheme(
                    headingTextColor = Color.parseColor("#121212"),
                    primaryTextColor = Color.parseColor("#2B2B2B"),
                    secondaryTextColor = Color.parseColor("#5A5A5A"),
                    tertiaryTextColor = Color.parseColor("#7A7A7A"),
                    quaternaryTextColor = Color.parseColor("#9A9A9A"),
            ),
            viewGroupTheme = ViewGroupTheme(
                    background = Color.parseColor("#ffffff"),
                    viewerBackground = Color.parseColor("#FAFAFA"),
                    highlightBackground = Color.parseColor("#F6F6F6"),
                    selectedBackground = Color.parseColor("#F1F1F1"),
                    dividerBackground = Color.parseColor("#DDDDDD")
            ),
            switchViewTheme = SwitchViewTheme(
                    switchOffColor = Color.parseColor("#F4F4F4")
            ),
            iconTheme = IconTheme(
                    regularIconColor = Color.parseColor("#2E2E2E"),
                    secondaryIconColor = Color.parseColor("#B1B1B1")
            )
    ),

    SOAPSTONE(
            textViewTheme = TextViewTheme(
                    headingTextColor = Color.parseColor("#161813"),
                    primaryTextColor = Color.parseColor("#1e201b"),
                    secondaryTextColor = Color.parseColor("#5A5A5A"),
                    tertiaryTextColor = Color.parseColor("#7A7A7A"),
                    quaternaryTextColor = Color.parseColor("#9A9A9A"),
            ),
            viewGroupTheme = ViewGroupTheme(
                    background = Color.parseColor("#fbfdf8"),
                    viewerBackground = Color.parseColor("#FAFAFA"),
                    highlightBackground = Color.parseColor("#F6F6F6"),
                    selectedBackground = Color.parseColor("#F1F1F1"),
                    dividerBackground = Color.parseColor("#767873")
            ),
            switchViewTheme = SwitchViewTheme(
                    switchOffColor = Color.parseColor("#F4F4F4")
            ),
            iconTheme = IconTheme(
                    regularIconColor = Color.parseColor("#2E2E2E"),
                    secondaryIconColor = Color.parseColor("#B1B1B1")
            )
    ),

    DARK(
            textViewTheme = TextViewTheme(
                    headingTextColor = Color.parseColor("#F1F1F1"),
                    primaryTextColor = Color.parseColor("#E4E4E4"),
                    secondaryTextColor = Color.parseColor("#C8C8C8"),
                    tertiaryTextColor = Color.parseColor("#AAAAAA"),
                    quaternaryTextColor = Color.parseColor("#9A9A9A"),
            ),
            viewGroupTheme = ViewGroupTheme(
                    background = Color.parseColor("#171717"),
                    viewerBackground = Color.parseColor("#404040"),
                    highlightBackground = Color.parseColor("#404040"),
                    selectedBackground = Color.parseColor("#242424"),
                    dividerBackground = Color.parseColor("#666666")
            ),
            switchViewTheme = SwitchViewTheme(
                    switchOffColor = Color.parseColor("#252525")
            ),
            iconTheme = IconTheme(
                    regularIconColor = Color.parseColor("#F8F8F8"),
                    secondaryIconColor = Color.parseColor("#E8E8E8")
            )
    ),

    AMOLED(
            textViewTheme = TextViewTheme(
                    headingTextColor = Color.parseColor("#F1F1F1"),
                    primaryTextColor = Color.parseColor("#E4E4E4"),
                    secondaryTextColor = Color.parseColor("#C8C8C8"),
                    tertiaryTextColor = Color.parseColor("#AAAAAA"),
                    quaternaryTextColor = Color.parseColor("#9A9A9A"),
            ),
            viewGroupTheme = ViewGroupTheme(
                    background = Color.parseColor("#000000"),
                    viewerBackground = Color.parseColor("#2D2D2D"),
                    highlightBackground = Color.parseColor("#404040"),
                    selectedBackground = Color.parseColor("#242424"),
                    dividerBackground = Color.parseColor("#666666")
            ),
            switchViewTheme = SwitchViewTheme(
                    switchOffColor = Color.parseColor("#252525")
            ),
            iconTheme = IconTheme(
                    regularIconColor = Color.parseColor("#F8F8F8"),
                    secondaryIconColor = Color.parseColor("#E8E8E8")
            )
    ),

    SLATE(
            textViewTheme = TextViewTheme(
                    headingTextColor = Color.parseColor("#F1F1F1"),
                    primaryTextColor = Color.parseColor("#E4E4E4"),
                    secondaryTextColor = Color.parseColor("#C8C8C8"),
                    tertiaryTextColor = Color.parseColor("#AAAAAA"),
                    quaternaryTextColor = Color.parseColor("#9A9A9A"),
            ),
            viewGroupTheme = ViewGroupTheme(
                    background = Color.parseColor("#20272e"),
                    viewerBackground = Color.parseColor("#223343"),
                    highlightBackground = Color.parseColor("#223343"),
                    selectedBackground = Color.parseColor("#273f58"),
                    dividerBackground = Color.parseColor("#666666")
            ),
            switchViewTheme = SwitchViewTheme(
                    switchOffColor = Color.parseColor("#314152")
            ),
            iconTheme = IconTheme(
                    regularIconColor = Color.parseColor("#F8F8F8"),
                    secondaryIconColor = Color.parseColor("#E8E8E8")
            )
    ),

    OIL(
            textViewTheme = TextViewTheme(
                    headingTextColor = Color.parseColor("#e6e6e6"),
                    primaryTextColor = Color.parseColor("#f6f8f5"),
                    secondaryTextColor = Color.parseColor("#C8C8C8"),
                    tertiaryTextColor = Color.parseColor("#AAAAAA"),
                    quaternaryTextColor = Color.parseColor("#9A9A9A"),
            ),
            viewGroupTheme = ViewGroupTheme(
                    background = Color.parseColor("#1A1C1B"),
                    viewerBackground = Color.parseColor("#223343"),
                    highlightBackground = Color.parseColor("#2a332e"),
                    selectedBackground = Color.parseColor("#232e28"),
                    dividerBackground = Color.parseColor("#304539")
            ),
            switchViewTheme = SwitchViewTheme(
                    switchOffColor = Color.parseColor("#1d2621")
            ),
            iconTheme = IconTheme(
                    regularIconColor = Color.parseColor("#F8F8F8"),
                    secondaryIconColor = Color.parseColor("#E8E8E8")
            )
    ),

    HIGH_CONTRAST(
            textViewTheme = TextViewTheme(
                    headingTextColor = Color.parseColor("#ffffff"),
                    primaryTextColor = Color.parseColor("#ffffff"),
                    secondaryTextColor = Color.parseColor("#ffffff"),
                    tertiaryTextColor = Color.parseColor("#ffffff"),
                    quaternaryTextColor = Color.parseColor("#ffffff"),
            ),
            viewGroupTheme = ViewGroupTheme(
                    background = Color.parseColor("#000000"),
                    viewerBackground = Color.parseColor("#000000"),
                    highlightBackground = Color.parseColor("#404040"),
                    selectedBackground = Color.parseColor("#242424"),
                    dividerBackground = Color.parseColor("#ffffff")
            ),
            switchViewTheme = SwitchViewTheme(
                    switchOffColor = Color.parseColor("#252525")
            ),
            iconTheme = IconTheme(
                    regularIconColor = Color.parseColor("#ffffff"),
                    secondaryIconColor = Color.parseColor("#E8E8E8")
            )
    ),

    MATERIAL_YOU_LIGHT(
            textViewTheme = TextViewTheme(
                    headingTextColor = MaterialYou.headingTextColor,
                    primaryTextColor = MaterialYou.primaryTextColor,
                    secondaryTextColor = MaterialYou.secondaryTextColor,
                    tertiaryTextColor = MaterialYou.tertiaryTextColor,
                    quaternaryTextColor = MaterialYou.quaternaryTextColor,
            ),
            viewGroupTheme = ViewGroupTheme(
                    background = MaterialYou.background,
                    viewerBackground = MaterialYou.viewerBackground,
                    highlightBackground = MaterialYou.highlightBackground,
                    selectedBackground = MaterialYou.selectedBackground,
                    dividerBackground = MaterialYou.dividerBackground
            ),
            switchViewTheme = SwitchViewTheme(
                    switchOffColor = MaterialYou.switchOffColor
            ),
            iconTheme = IconTheme(
                    regularIconColor = MaterialYou.regularIconColor,
                    secondaryIconColor = MaterialYou.secondaryIconColor
            )
    ),

    MATERIAL_YOU_DARK(
            textViewTheme = TextViewTheme(
                    headingTextColor = MaterialYou.headingTextColorDark,
                    primaryTextColor = MaterialYou.primaryTextColorDark,
                    secondaryTextColor = MaterialYou.secondaryTextColorDark,
                    tertiaryTextColor = MaterialYou.tertiaryTextColorDark,
                    quaternaryTextColor = MaterialYou.quaternaryTextColorDark,
            ),
            viewGroupTheme = ViewGroupTheme(
                    background = MaterialYou.backgroundDark,
                    viewerBackground = MaterialYou.viewerBackgroundDark,
                    highlightBackground = MaterialYou.highlightBackgroundDark,
                    selectedBackground = MaterialYou.selectedBackgroundDark,
                    dividerBackground = MaterialYou.dividerBackgroundDark
            ),
            switchViewTheme = SwitchViewTheme(
                    switchOffColor = MaterialYou.switchOffColorDark
            ),
            iconTheme = IconTheme(
                    regularIconColor = MaterialYou.regularIconColorDark,
                    secondaryIconColor = MaterialYou.secondaryIconColorDark
            )
    )
}