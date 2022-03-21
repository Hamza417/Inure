package app.simple.inure.util

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import app.simple.inure.R

object TypeFace {

    enum class TypefaceStyle(val style: Int) {
        LIGHT(0),
        REGULAR(1),
        MEDIUM(2),
        BOLD(3)
    }

    /**
     * [AUTO] is default as it is appropriate
     * to let the initial font to be of user's
     * choice unless he specifies his preferences
     * in later stages.
     */
    const val AUTO = "auto"
    private const val PLUS_JAKARTA = "plus_jakarta"
    private const val LATO = "lato"
    private const val MULISH = "mulish"
    private const val JOST = "jost"
    private const val EPILOGUE = "epilogue"
    private const val UBUNTU = "ubuntu"
    private const val POPPINS = "poppins"
    private const val MANROPE = "manrope"
    private const val INTER = "inter"
    private const val OVERPASS = "overpass"
    private const val URBANIST = "urbanist"
    private const val NUNITO = "nunito"
    private const val OSWALD = "oswald"
    private const val ROBOTO = "roboto"
    private const val REFORMA = "reforma"
    private const val SUBJECTIVITY = "subjectivity"
    private const val MOHAVE = "mohave"
    private const val YESSICA = "yessica"
    private const val AUDREY = "audrey"
    private const val JOSEFIN = "josefin_sans"
    private const val COMFORTAA = "comfortaa"
    private const val CHILLAX = "chillax"
    private const val BONNY = "bonny"
    private const val SOURCE_SANS_PRO = "source_sans_pro"
    private const val FREDOKA = "fredoka"
    private const val HEEBO = "heebo"
    private const val MALI = "mali"

    fun getTypeFace(appFont: String, style: Int, context: Context): Typeface? {
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
            CHILLAX -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.chillax_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.chillax_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.chillax_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.chillax_semi_bold)
                    }
                }
            }
            BONNY -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.bonny_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.bonny_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.bonny_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.bonny_bold)
                    }
                }
            }
            SOURCE_SANS_PRO -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.source_code_pro_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.source_code_pro_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.source_code_pro_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.source_code_pro_bold)
                    }
                }
            }
            FREDOKA -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.fredoka_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.fredoka_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.fredoka_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.fredoka_bold)
                    }
                }
            }
            HEEBO -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.heebo_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.heebo_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.heebo_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.heebo_bold)
                    }
                }
            }
            MALI -> {
                when (style) {
                    0 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mali_light)
                    }
                    1 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mali_regular)
                    }
                    2 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mali_medium)
                    }
                    3 -> {
                        typeface = ResourcesCompat.getFont(context, R.font.mali_bold)
                    }
                }
            }
        }

        return typeface
    }

    /**
     * List of all typefaces with their code names and red IDs
     */
    val list: ArrayList<TypeFaceModel> = arrayListOf(
            TypeFaceModel("Auto (System Default)", 0, AUTO),
            TypeFaceModel("Lato", R.font.lato_bold, LATO),
            TypeFaceModel("Plus Jakarta Sans", R.font.plus_jakarta_bold, PLUS_JAKARTA),
            TypeFaceModel("Mulish", R.font.mulish_bold, MULISH),
            TypeFaceModel("Jost", R.font.jost_bold, JOST),
            TypeFaceModel("Epilogue", R.font.epilogue_bold, EPILOGUE),
            TypeFaceModel("Ubuntu", R.font.ubuntu_bold, UBUNTU),
            TypeFaceModel("Poppins", R.font.poppins_bold, POPPINS),
            TypeFaceModel("Manrope", R.font.manrope_bold, MANROPE),
            TypeFaceModel("Inter", R.font.inter_bold, INTER),
            TypeFaceModel("Overpass", R.font.overpass_bold, OVERPASS),
            TypeFaceModel("Urbanist", R.font.urbanist_bold, URBANIST),
            TypeFaceModel("Nunito", R.font.nunito_bold, NUNITO),
            TypeFaceModel("Oswald", R.font.oswald_bold, OSWALD),
            TypeFaceModel("Roboto", R.font.roboto_bold, ROBOTO),
            TypeFaceModel("Reforma", R.font.reforma_negra, REFORMA),
            TypeFaceModel("Subjectivity", R.font.subjectivity_bold, SUBJECTIVITY),
            TypeFaceModel("Mohave", R.font.mohave_bold, MOHAVE),
            TypeFaceModel("Yessica", R.font.yessica_bold, YESSICA),
            TypeFaceModel("Audrey", R.font.audrey_bold, AUDREY),
            TypeFaceModel("Josefin Sans", R.font.josefin_sans_bold, JOSEFIN),
            TypeFaceModel("Comfortaa", R.font.comfortaa_bold, COMFORTAA),
            TypeFaceModel("Chillax", R.font.chillax_semi_bold, CHILLAX),
            TypeFaceModel("Bonny", R.font.bonny_bold, BONNY),
            TypeFaceModel("SourceSans Pro", R.font.source_code_pro_bold, SOURCE_SANS_PRO),
            TypeFaceModel("Fredoka", R.font.fredoka_bold, FREDOKA),
            TypeFaceModel("Heebo", R.font.heebo_bold, HEEBO),
            TypeFaceModel("Mali", R.font.mali_bold, MALI)
    )

    class TypeFaceModel(
            /**
             * Proper name of the typeface such as [ROBOTO]
             */
            val typefaceName: String,

            /**
             * Resource ID for the type face that is used to set typeface
             */
            val typeFaceResId: Int,

            /**
             * Name of the typeface that is used by the
             * preference manager of the app to identify
             * which typeface is used similar to [typefaceName]
             * except it is all lowercase
             */
            val name: String,
    )
}
