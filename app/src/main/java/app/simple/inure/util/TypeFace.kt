package app.simple.inure.util

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import app.simple.inure.R

object TypeFace {

    const val AUTO = "auto"
    const val PLUS_JAKARTA = "plus_jakarta"
    const val LATO = "lato"
    const val MULISH = "mulish"
    const val JOST = "jost"
    const val EPILOGUE = "epilogue"
    const val UBUNTU = "ubuntu"
    const val POPPINS = "poppins"
    const val MANROPE = "manrope"
    const val INTER = "inter"
    const val OVERPASS = "overpass"
    const val URBANIST = "urbanist"
    const val NUNITO = "nunito"
    const val OSWALD = "oswald"
    const val ROBOTO = "roboto"
    const val REFORMA = "reforma"
    const val SUBJECTIVITY = "subjectivity"
    const val MOHAVE = "mohave"
    const val YESSICA = "yessica"
    const val AUDREY = "audrey"
    const val JOSEFIN = "josefin_sans"
    const val COMFORTAA = "comfortaa"

    fun getTypeFace(appFont: String, style: Int, context: Context): Typeface {
        var typeface: Typeface? = null

        when (appFont) {
            AUTO -> {
                when (style) {
                    0, 1 -> {
                        typeface = Typeface.DEFAULT
                    }
                    2, 3 -> {
                        typeface = Typeface.DEFAULT_BOLD
                    }
                }
            }
            LATO -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.lato_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.lato_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.lato_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.lato_bold)
                    }
                }
            }
            PLUS_JAKARTA -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.plus_jakarta_bold)
                    }
                }
            }
            MULISH -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mulish_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mulish_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mulish_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mulish_bold)
                    }
                }
            }
            JOST -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.jost_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.jost_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.jost_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.jost_bold)
                    }
                }
            }
            EPILOGUE -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.epilogue_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.epilogue_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.epilogue_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.epilogue_bold)
                    }
                }
            }
            UBUNTU -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.ubuntu_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.ubuntu_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.ubuntu_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.ubuntu_bold)
                    }
                }
            }
            POPPINS -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.poppins_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.poppins_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.poppins_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.poppins_bold)
                    }
                }
            }
            MANROPE -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.manrope_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.manrope_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.manrope_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.manrope_bold)
                    }
                }
            }
            INTER -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.inter_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.inter_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.inter_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.inter_bold)
                    }
                }
            }
            OVERPASS -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.overpass_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.overpass_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.overpass_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.overpass_bold)
                    }
                }
            }
            URBANIST -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.urbanist_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.urbanist_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.urbanist_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.urbanist_bold)
                    }
                }
            }
            NUNITO -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.nunito_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.nunito_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.nunito_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.nunito_bold)
                    }
                }
            }
            OSWALD -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.oswald_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.oswald_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.oswald_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.oswald_bold)
                    }
                }
            }
            ROBOTO -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.roboto_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.roboto_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.roboto_bold)
                    }
                }
            }
            REFORMA -> {
                when (style) {
                    0,
                    1,
                    -> {
                        typeface = ResourcesCompat.getFont(context, R.font.reforma_blanca)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.reforma_gris)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.reforma_negra)
                    }
                }
            }
            SUBJECTIVITY -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.subjectivity_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.subjectivity_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.subjectivity_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.subjectivity_bold)
                    }
                }
            }
            MOHAVE -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mohave_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mohave_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mohave_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mohave_bold)
                    }
                }
            }
            YESSICA -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.yessica_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.yessica_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.yessica_regular)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.yessica_bold)
                    }
                }
            }
            AUDREY -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.audrey_regular)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.audrey_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.audrey_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.audrey_bold)
                    }
                }
            }
            JOSEFIN -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.josefin_sans_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.josefin_sans_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.josefin_sans_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.josefin_sans_bold)
                    }
                }
            }
            COMFORTAA -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.comfortaa_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.comfortaa_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.comfortaa_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.comfortaa_bold)
                    }
                }
            }
        }

        return typeface!!
    }
}
