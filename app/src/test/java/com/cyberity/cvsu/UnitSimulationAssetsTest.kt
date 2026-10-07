package com.cyberity.cvsu

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class UnitSimulationAssetsTest {
    @Test
    fun groupedUnitAssetsResolvePagesAndLocalDependencies() {
        val assets = listOf(File("src/main/assets"), File("app/src/main/assets"))
            .first { it.isDirectory }
        val directories = mapOf(
            101 to "UNIT 01/inbox_triage", 102 to "UNIT 01/threat_console",
            103 to "UNIT 01/cia_triad", 104 to "UNIT 01/hardening_review",
            106 to "UNIT 01/risk_register", 301 to "UNIT 03/phish_desk",
            302 to "UNIT 03/bait_workshop", 303 to "UNIT 03/social_eng",
            304 to "UNIT 03/scam_messages", 305 to "UNIT 03/one_day",
            350 to "UNIT 03/quick_quiz", 401 to "UNIT 04/quarantine_vault", 402 to "UNIT 04/org_flashdrive",
            403 to "UNIT 04/ransom_night", 404 to "UNIT 04/lab3_hardening",
            450 to "UNIT 04/bonus_cache",
            405 to "UNIT 04/malware_triage",
            501 to "UNIT 05/network_basics",
            502 to "UNIT 05/wifi_security",
            503 to "UNIT 05/safe_browsing",
            504 to "UNIT 05/network_threats",
            505 to "UNIT 05/firewall_ops",
            550 to "UNIT 05/rogue_ap",
            601 to "UNIT 06/incident_desk",
            602 to "UNIT 06/detection_console",
            603 to "UNIT 06/containment_ops",
            604 to "UNIT 06/recovery_room",
            605 to "UNIT 06/ir_simulation"
        )
        for ((levelId, directory) in directories) {
            val lab = (contentFor(levelId) as LevelContent.Lab).lab
            assertEquals(directory, lab.assetDir)
            val folder = File(assets, "simulations/$directory")
            for (page in listOf(lab.startPage) + lab.tasks.mapNotNull { it.entryPage }) {
                assertTrue("Missing entry page for level $levelId: $page",
                    File(folder, page.substringBefore('?').substringBefore('#')).isFile)
            }
            folder.walkTopDown().filter { it.extension == "html" }.forEach { page ->
                // Inline scripts include fabricated page source as phishing evidence.
                val markup = Regex("(?s)(<script\\b[^>]*>).*?</script>")
                    .replace(page.readText(), "$1</script>")
                Regex("(?:src|href)=\"([^\"]+)\"").findAll(markup).forEach { match ->
                    val resource = match.groupValues[1].substringBefore('?').substringBefore('#')
                    if (resource.isNotEmpty() && !resource.contains(":")) {
                        assertTrue("Missing dependency in ${page.name}: $resource",
                            File(page.parentFile, resource).isFile)
                    }
                }
            }
        }
        assertTrue(contentFor(105) is LevelContent.Scenarios)
    }
}
