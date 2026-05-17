package com.lostandfound.data.services

import com.lostandfound.data.models.SecurityQuestion
import java.util.UUID

/**
 * Phone-specific question generator.
 */
class PhoneQuestionGenerator : CategoryQuestionGenerator {
    
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["brand_model"]?.let { brandModel ->
            if (brandModel.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the brand and model of this phone?",
                    category = category,
                    hiddenDetailKey = "brand_model"
                ))
            }
        }
        
        hiddenDetails["wallpaper"]?.let { wallpaper ->
            if (wallpaper.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe the lock screen wallpaper or background image.",
                    category = category,
                    hiddenDetailKey = "wallpaper"
                ))
            }
        }
        
        hiddenDetails["case_type"]?.let { caseType ->
            if (caseType.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What type and color of case is on this phone?",
                    category = category,
                    hiddenDetailKey = "case_type"
                ))
            }
        }
        
        hiddenDetails["home_apps"]?.let { apps ->
            if (apps.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What apps are visible on the home screen?",
                    category = category,
                    hiddenDetailKey = "home_apps"
                ))
            }
        }
        
        hiddenDetails["phone_digits"]?.let { digits ->
            if (digits.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What are the last 4 digits of the phone number?",
                    category = category,
                    hiddenDetailKey = "phone_digits"
                ))
            }
        }
        
        hiddenDetails["carrier"]?.let { carrier ->
            if (carrier.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What mobile carrier does this phone use?",
                    category = category,
                    hiddenDetailKey = "carrier"
                ))
            }
        }
        
        return questions.take(3)
    }
}

/**
 * Wallet-specific question generator.
 */
class WalletQuestionGenerator : CategoryQuestionGenerator {
    
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["card_types"]?.let { cardTypes ->
            if (cardTypes.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What types of cards are inside this wallet?",
                    category = category,
                    hiddenDetailKey = "card_types"
                ))
            }
        }
        
        hiddenDetails["cash_amount"]?.let { cashAmount ->
            if (cashAmount.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Approximately how much cash is in this wallet?",
                    category = category,
                    hiddenDetailKey = "cash_amount"
                ))
            }
        }
        
        hiddenDetails["color_material"]?.let { colorMaterial ->
            if (colorMaterial.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What color and material is this wallet made of?",
                    category = category,
                    hiddenDetailKey = "color_material"
                ))
            }
        }
        
        hiddenDetails["id_type"]?.let { idType ->
            if (idType.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What type of ID is in this wallet?",
                    category = category,
                    hiddenDetailKey = "id_type"
                ))
            }
        }
        
        hiddenDetails["brand"]?.let { brand ->
            if (brand.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What brand is this wallet?",
                    category = category,
                    hiddenDetailKey = "brand"
                ))
            }
        }
        
        return questions.take(3)
    }
}

/**
 * Keys-specific question generator.
 */
class KeysQuestionGenerator : CategoryQuestionGenerator {
    
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["keychain_details"]?.let { keychainDetails ->
            if (keychainDetails.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe the keychain (material, color, shape).",
                    category = category,
                    hiddenDetailKey = "keychain_details"
                ))
            }
        }
        
        hiddenDetails["key_count"]?.let { keyCount ->
            if (keyCount.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "How many keys are on this keychain?",
                    category = category,
                    hiddenDetailKey = "key_count"
                ))
            }
        }
        
        hiddenDetails["key_types"]?.let { keyTypes ->
            if (keyTypes.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What types of keys are included (house, car, office, etc.)?",
                    category = category,
                    hiddenDetailKey = "key_types"
                ))
            }
        }
        
        hiddenDetails["attached_items"]?.let { attachedItems ->
            if (attachedItems.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What items are attached to the keychain (fobs, tags, etc.)?",
                    category = category,
                    hiddenDetailKey = "attached_items"
                ))
            }
        }
        
        return questions.take(3)
    }
}

/**
 * Bag/Backpack-specific question generator.
 */
class BagQuestionGenerator : CategoryQuestionGenerator {
    
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["contents"]?.let { contents ->
            if (contents.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What are the main contents inside this bag?",
                    category = category,
                    hiddenDetailKey = "contents"
                ))
            }
        }
        
        hiddenDetails["brand"]?.let { brand ->
            if (brand.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What brand is this bag?",
                    category = category,
                    hiddenDetailKey = "brand"
                ))
            }
        }
        
        hiddenDetails["color_material"]?.let { colorMaterial ->
            if (colorMaterial.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What color and material is this bag?",
                    category = category,
                    hiddenDetailKey = "color_material"
                ))
            }
        }
        
        hiddenDetails["pockets_compartments"]?.let { pockets ->
            if (pockets.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "How many pockets or compartments does this bag have?",
                    category = category,
                    hiddenDetailKey = "pockets_compartments"
                ))
            }
        }
        
        return questions.take(3)
    }
}

/**
 * Laptop-specific question generator.
 */
class LaptopQuestionGenerator : CategoryQuestionGenerator {
    
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["brand_model"]?.let { brandModel ->
            if (brandModel.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the brand and model of this laptop?",
                    category = category,
                    hiddenDetailKey = "brand_model"
                ))
            }
        }
        
        hiddenDetails["stickers_decorations"]?.let { stickers ->
            if (stickers.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe any stickers, decorations, or markings on this laptop.",
                    category = category,
                    hiddenDetailKey = "stickers_decorations"
                ))
            }
        }
        
        hiddenDetails["screen_size"]?.let { screenSize ->
            if (screenSize.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the screen size of this laptop?",
                    category = category,
                    hiddenDetailKey = "screen_size"
                ))
            }
        }
        
        hiddenDetails["case_sleeve"]?.let { caseSleeve ->
            if (caseSleeve.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe the laptop case or sleeve.",
                    category = category,
                    hiddenDetailKey = "case_sleeve"
                ))
            }
        }
        
        return questions.take(3)
    }
}

/**
 * Additional category generators following the same pattern.
 */
class TabletQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["brand_model"]?.let { brandModel ->
            if (brandModel.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the brand and model of this tablet?",
                    category = category,
                    hiddenDetailKey = "brand_model"
                ))
            }
        }
        
        hiddenDetails["case_cover"]?.let { caseCover ->
            if (caseCover.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe the case or cover on this tablet.",
                    category = category,
                    hiddenDetailKey = "case_cover"
                ))
            }
        }
        
        hiddenDetails["wallpaper"]?.let { wallpaper ->
            if (wallpaper.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe the wallpaper or background image.",
                    category = category,
                    hiddenDetailKey = "wallpaper"
                ))
            }
        }
        
        return questions.take(3)
    }
}

class HeadphonesQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["brand_model"]?.let { brandModel ->
            if (brandModel.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the brand and model of these headphones?",
                    category = category,
                    hiddenDetailKey = "brand_model"
                ))
            }
        }
        
        hiddenDetails["type"]?.let { type ->
            if (type.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What type of headphones are these (over-ear, in-ear, wireless, etc.)?",
                    category = category,
                    hiddenDetailKey = "type"
                ))
            }
        }
        
        hiddenDetails["color"]?.let { color ->
            if (color.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What color are these headphones?",
                    category = category,
                    hiddenDetailKey = "color"
                ))
            }
        }
        
        return questions.take(3)
    }
}

class WatchQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["brand_model"]?.let { brandModel ->
            if (brandModel.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the brand and model of this watch?",
                    category = category,
                    hiddenDetailKey = "brand_model"
                ))
            }
        }
        
        hiddenDetails["band_material"]?.let { bandMaterial ->
            if (bandMaterial.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What material and color is the watch band?",
                    category = category,
                    hiddenDetailKey = "band_material"
                ))
            }
        }
        
        hiddenDetails["face_design"]?.let { faceDesign ->
            if (faceDesign.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe the watch face design or digital display.",
                    category = category,
                    hiddenDetailKey = "face_design"
                ))
            }
        }
        
        return questions.take(3)
    }
}

class JewelryQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["metal_type"]?.let { metalType ->
            if (metalType.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What type of metal is this jewelry made of?",
                    category = category,
                    hiddenDetailKey = "metal_type"
                ))
            }
        }
        
        hiddenDetails["stone_details"]?.let { stoneDetails ->
            if (stoneDetails.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe any stones, gems, or settings on this jewelry.",
                    category = category,
                    hiddenDetailKey = "stone_details"
                ))
            }
        }
        
        hiddenDetails["engravings"]?.let { engravings ->
            if (engravings.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What engravings or inscriptions are on this jewelry?",
                    category = category,
                    hiddenDetailKey = "engravings"
                ))
            }
        }
        
        return questions.take(3)
    }
}

class ClothingQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["brand"]?.let { brand ->
            if (brand.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What brand is this clothing item?",
                    category = category,
                    hiddenDetailKey = "brand"
                ))
            }
        }
        
        hiddenDetails["size"]?.let { size ->
            if (size.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What size is this clothing item?",
                    category = category,
                    hiddenDetailKey = "size"
                ))
            }
        }
        
        hiddenDetails["pockets_contents"]?.let { pocketsContents ->
            if (pocketsContents.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What pockets does this item have and what's in them?",
                    category = category,
                    hiddenDetailKey = "pockets_contents"
                ))
            }
        }
        
        return questions.take(3)
    }
}

class BooksQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["title_author"]?.let { titleAuthor ->
            if (titleAuthor.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the title and author of this book?",
                    category = category,
                    hiddenDetailKey = "title_author"
                ))
            }
        }
        
        hiddenDetails["bookmarks_notes"]?.let { bookmarksNotes ->
            if (bookmarksNotes.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe any bookmarks, notes, or highlighting in this book.",
                    category = category,
                    hiddenDetailKey = "bookmarks_notes"
                ))
            }
        }
        
        hiddenDetails["cover_type"]?.let { coverType ->
            if (coverType.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What type of cover does this book have?",
                    category = category,
                    hiddenDetailKey = "cover_type"
                ))
            }
        }
        
        return questions.take(3)
    }
}

class IdDocumentsQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["document_type"]?.let { documentType ->
            if (documentType.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What type of document is this?",
                    category = category,
                    hiddenDetailKey = "document_type"
                ))
            }
        }
        
        hiddenDetails["issuing_authority"]?.let { issuingAuthority ->
            if (issuingAuthority.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What authority or state issued this document?",
                    category = category,
                    hiddenDetailKey = "issuing_authority"
                ))
            }
        }
        
        hiddenDetails["expiration_info"]?.let { expirationInfo ->
            if (expirationInfo.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What is the expiration year or month of this document?",
                    category = category,
                    hiddenDetailKey = "expiration_info"
                ))
            }
        }
        
        return questions.take(3)
    }
}

class GlassesQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["frame_type"]?.let { frameType ->
            if (frameType.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What type and material are the frames?",
                    category = category,
                    hiddenDetailKey = "frame_type"
                ))
            }
        }
        
        hiddenDetails["lens_type"]?.let { lensType ->
            if (lensType.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What type of lenses are these (prescription, sunglasses, etc.)?",
                    category = category,
                    hiddenDetailKey = "lens_type"
                ))
            }
        }
        
        hiddenDetails["brand"]?.let { brand ->
            if (brand.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What brand are these glasses?",
                    category = category,
                    hiddenDetailKey = "brand"
                ))
            }
        }
        
        return questions.take(3)
    }
}

class UmbrellaQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["color_pattern"]?.let { colorPattern ->
            if (colorPattern.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What color and pattern is this umbrella?",
                    category = category,
                    hiddenDetailKey = "color_pattern"
                ))
            }
        }
        
        hiddenDetails["size_type"]?.let { sizeType ->
            if (sizeType.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What size and type of umbrella is this?",
                    category = category,
                    hiddenDetailKey = "size_type"
                ))
            }
        }
        
        hiddenDetails["handle_material"]?.let { handleMaterial ->
            if (handleMaterial.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What material and design is the handle?",
                    category = category,
                    hiddenDetailKey = "handle_material"
                ))
            }
        }
        
        return questions.take(3)
    }
}

class WaterBottleQuestionGenerator : CategoryQuestionGenerator {
    override fun createQuestions(hiddenDetails: Map<String, String>, category: String): List<SecurityQuestion> {
        val questions = mutableListOf<SecurityQuestion>()
        
        hiddenDetails["brand"]?.let { brand ->
            if (brand.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What brand is this water bottle?",
                    category = category,
                    hiddenDetailKey = "brand"
                ))
            }
        }
        
        hiddenDetails["stickers_decorations"]?.let { stickersDecorations ->
            if (stickersDecorations.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "Describe any stickers, decorations, or markings on this bottle.",
                    category = category,
                    hiddenDetailKey = "stickers_decorations"
                ))
            }
        }
        
        hiddenDetails["material"]?.let { material ->
            if (material.isNotBlank()) {
                questions.add(SecurityQuestion(
                    id = generateId(),
                    question = "What material is this water bottle made of?",
                    category = category,
                    hiddenDetailKey = "material"
                ))
            }
        }
        
        return questions.take(3)
    }
}

private fun generateId(): String = UUID.randomUUID().toString()