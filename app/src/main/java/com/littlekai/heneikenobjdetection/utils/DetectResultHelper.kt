package com.littlekai.heneikenobjdetection.utils

import android.graphics.RectF
import android.util.Log
import androidx.preference.PreferenceManager
import com.littlekai.heneikenobjdetection.dao.HenikenObjDetectionApplication
import com.littlekai.heneikenobjdetection.model.Label
import org.tensorflow.lite.task.vision.detector.Detection

class DetectResultHelper {

    companion object {
        val CONTEXT_LABELS: Set<String> = setOf(
            "advertise",
            "dining",
            "drinking",
            "events",
            "friends",
            "grocery_store",
            "outlet",
            "restaurant",
            "supermarket",
            "outdoor",
            "celebration",
            "party"
        )
        val EXCEPT_LABELS: Set<String> = setOf("beer_box")

        fun preferenceLabelFilteredResults(results: List<Detection>?): MutableList<Detection> {
            if (results != null) {
                if (results.isEmpty()) {
                    return mutableListOf() // Return empty list if results is empty
                }
                val context = HenikenObjDetectionApplication.getAppContext()
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val selectedValues = sharedPreferences.getStringSet("list_label", HashSet())

                val filteredResults = mutableListOf<Detection>()
                for (result in results) {
                    if (selectedValues != null) {
                        if (selectedValues.contains(result.categories[0].label) || result.categories[0].label.contains(
                                "beer_box"
                            )
                        ) {
                            filteredResults.add(result)
                        }
                    }
                }
                return filteredResults
            }
            return mutableListOf()
        }


        fun detectContextResults1(results: List<Detection>?): String {
            if (results == null) {
                return "There is no object detected"
            }
            var result_string = ""

            if (results.isNotEmpty()) {
                var context_string = " - <b><u><font color='red'>Context:</font></u></b><br>"
                var allResultText = ""
                var face_num = 0
                var human_num = 0
                for (i in 0 until results.size) {
                    var label = results[i].categories[0].label


                    when (label) {
                        "face" -> face_num++ //count face number
                        "human" -> human_num++ //count human number
                        "PG" -> human_num++ //count human number
                    }

                    // add scene context
                    if (CONTEXT_LABELS.contains(label)) {
                        if (label == "outlet") // fix label name
                            label = "outdoors"

                        context_string =
                            context_string + label + " (" + (results[i].categories[0].score * 100).toInt() + "%); "
                    } else allResultText =
                        allResultText + label + " (" + (results[i].categories[0].score * 100).toInt() + "%); "
                }
                if (face_num > 0 || human_num > 0) result_string += if (face_num >= human_num) {
                    " - <b><u><font color='red'>Number of people detected:</font></u></b> $face_num<br>"
                } else {
                    " - <b><u><font color='red'>Number of people detected:</font></u></b> $human_num<br>"
                }
                    result_string += context_string
                if(!context_string.contains("outdoor")) result_string += "; indoors"

                result_string += "<br> - <b><u><font color='red'>All results:</font></u></b><br> $allResultText"

                return result_string
            }
            return "There is no object detected"
        }


        fun getLabelBrand(tf_detects: MutableList<Detection>): ArrayList<Label> {
            val labels = ArrayList<Label>()
            val brandedLabel = ArrayList<Label>()
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(HenikenObjDetectionApplication.getAppContext())
            if (sharedPreferences.getBoolean("obj_brand", false)) {

                var temp = updateBillboardAndPosterNames(tf_detects)
                if (temp.isNotEmpty()) brandedLabel.addAll(temp)
                temp = updatePGNames(tf_detects)
                if (temp.isNotEmpty()) brandedLabel.addAll(temp)
                temp = updateDrinkerNames(tf_detects)
                if (temp.isNotEmpty()) brandedLabel.addAll(temp)

            }

//
            for (tfdetect in tf_detects) labels.add(
                Label(
                    tfdetect.categories[0].label, tfdetect.boundingBox, tfdetect.categories[0].score
                )
            )
            if (brandedLabel.isNotEmpty()) labels.addAll(brandedLabel)

            return labels
        }

        fun updateBillboardAndPosterNames(elements: MutableList<Detection>): ArrayList<Label> {
            val billboards =
                elements.filter { it.categories[0].label.startsWith("billboard") }.toMutableList()
            val posters =
                elements.filter { it.categories[0].label.startsWith("poster") }.toMutableList()
            if (billboards.isEmpty() && posters.isEmpty())
                return ArrayList()
            val beersAndLogos = elements.filter {
                it.categories[0].label.startsWith("beer_") || it.categories[0].label.startsWith("logo_")
            }.toMutableList()

            val labels = ArrayList<Label>()

            for (beerOrLogo in beersAndLogos) {
                val intersectedBillboard =
                    billboards.find { isInside(it.boundingBox, beerOrLogo.boundingBox) }
                val intersectedPoster =
                    posters.find { isInside(it.boundingBox, beerOrLogo.boundingBox) }
                val brandName = beerOrLogo.categories[0].label.split('_').last()

                if (intersectedBillboard != null) {
                    labels.add(
                        Label(
                            "billboard_$brandName",
                            intersectedBillboard.boundingBox,
                            intersectedBillboard.categories[0].score
                        )
                    )
//                    elements.remove(beerOrLogo)
                    elements.remove(intersectedBillboard)
                    billboards.remove(intersectedBillboard)
                }
                if (intersectedPoster != null) {
                    labels.add(
                        Label(
                            "poster_$brandName",
                            intersectedPoster.boundingBox,
                            intersectedPoster.categories[0].score
                        )
                    )
//                    elements.remove(beerOrLogo)
                    elements.remove(intersectedPoster)
                    posters.remove(intersectedPoster)
                }
            }
            return labels
        }

        fun updatePGNames(elements: MutableList<Detection>): ArrayList<Label> {
            val PGs =
                elements.filter { it.categories[0].label.startsWith("PG") }.toMutableList()
            if (PGs.isEmpty())
                return ArrayList()
            val Logos = elements.filter {
                it.categories[0].label.startsWith("logo_")
            }.toMutableList()

            val labels = ArrayList<Label>()

            for (logo in Logos) {
                val intersectedPG =
//                    PGs.find { RectF.intersects(it.boundingBox, logo.boundingBox) }
                    PGs.find { isInside(it.boundingBox, logo.boundingBox) }

                val brandName = logo.categories[0].label.split('_').last()

                if (intersectedPG != null) {
                    labels.add(
                        Label(
                            "PG_$brandName",
                            intersectedPG.boundingBox,
                            intersectedPG.categories[0].score
                        )
                    )
//                    elements.remove(logo)
                    elements.remove(intersectedPG)
                    PGs.remove(intersectedPG)
                }

            }
            return labels
        }


        private fun updateDrinkerNames(elements: MutableList<Detection>): ArrayList<Label> {
            val friends = elements.filter { it.categories[0].label.startsWith("friends") }.toMutableList()
            val humans = elements.filter { it.categories[0].label.startsWith("human") }.toMutableList()

            Log.d("Detect Result Helper", "${friends.size}: ${humans.size}")

            if (friends.isEmpty() || humans.isEmpty()) {
                return ArrayList()
            }

            val labels = ArrayList<Label>()
            val processedHumans = mutableListOf<Detection>()
            val processedFriends = mutableListOf<Detection>()

            for (friend in friends) {
                for (human in humans) {
                    if (isInside(friend.boundingBox, human.boundingBox)) {
                        Log.d("Detect Result Helper", "get Drinker:")
                        labels.add(
                            Label(
                                "drinker",
                                human.boundingBox,
                                human.categories[0].score
                            )
                        )
                        processedHumans.add(human)
//                        break
                    }
                }
            }

            // Remove processed humans and friends from elements
            elements.removeAll(processedHumans)
            elements.removeAll(processedFriends)

            return labels
        }


        private fun isInside(rectB: RectF, rectA: RectF): Boolean {
            if (rectB.contains(rectA))
                return true
            return isMoreThanHalfAreaInside(rectA, rectB)
        }

        private fun isMoreThanHalfAreaInside(A: RectF, B: RectF): Boolean {
            // Calculate the intersection of A and B
            val intersection = RectF()
            val intersects = intersection.setIntersect(A, B)
            Log.d("Detect Result Helper", "intersects:" + intersects)

            // If there is no intersection, return false
            if (!intersects) return false

            // Calculate the area of A
            val areaA = A.width() * A.height()

            // Calculate the area of the intersection
            val areaIntersection = intersection.width() * intersection.height()
            Log.d("Detect Result Helper", areaA.toString() + ":" + areaIntersection)
            // Check if the intersection area is more than half of the area of A
            return areaIntersection > (areaA / 2)
        }

    }
}
