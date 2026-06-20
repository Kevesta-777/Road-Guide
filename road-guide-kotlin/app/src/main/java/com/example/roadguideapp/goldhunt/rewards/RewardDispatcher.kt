package com.example.roadguideapp.goldhunt.rewards

import com.example.roadguideapp.goldhunt.engine.GridCell
import java.security.MessageDigest

internal object RewardDispatcher {
    private const val L1_CLUSTER_MIN_CELLS = 3
    private const val L0_CREDIT = 1
    private const val L1_CLUSTER_CREDIT = 5
    private const val DISTRICT_CREDIT = 10

    fun grantsForNewL0(
        cell: GridCell,
        l1CellId: String?,
        l1ExploredCount: Int,
        l1ClusterAlreadyGranted: Boolean,
        districtIsNew: Boolean,
        districtKey: String?,
    ): List<CreditGrant> {
        val grants = ArrayList<CreditGrant>(3)
        grants += CreditGrant(
            eventId = stableEventId(RewardRuleType.CELL_L0, cell.id),
            ruleType = RewardRuleType.CELL_L0,
            amount = L0_CREDIT,
            label = cell.id,
        )
        if (
            l1CellId != null &&
            !l1ClusterAlreadyGranted &&
            l1ExploredCount >= L1_CLUSTER_MIN_CELLS
        ) {
            grants += CreditGrant(
                eventId = stableEventId(RewardRuleType.CLUSTER_L1, l1CellId),
                ruleType = RewardRuleType.CLUSTER_L1,
                amount = L1_CLUSTER_CREDIT,
                label = l1CellId,
            )
        }
        if (districtIsNew && districtKey != null) {
            grants += CreditGrant(
                eventId = stableEventId(RewardRuleType.DISTRICT_L2, districtKey),
                ruleType = RewardRuleType.DISTRICT_L2,
                amount = DISTRICT_CREDIT,
                label = districtKey,
            )
        }
        return grants
    }

    private fun stableEventId(rule: String, target: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$rule:$target".toByteArray())
        return bytes.take(16).joinToString("") { "%02x".format(it) }
    }
}
