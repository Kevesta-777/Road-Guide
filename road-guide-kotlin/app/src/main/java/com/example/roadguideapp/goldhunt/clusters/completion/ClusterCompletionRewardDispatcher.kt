package com.example.roadguideapp.goldhunt.clusters.completion

import com.example.roadguideapp.goldhunt.clusters.TreasureCluster
import com.example.roadguideapp.goldhunt.rewards.CreditGrant
import com.example.roadguideapp.goldhunt.rewards.RewardRuleType

internal object ClusterCompletionRewardDispatcher {
    fun grantFor(cluster: TreasureCluster): CreditGrant = CreditGrant(
        eventId = "credit:cluster_complete:${cluster.clusterId}",
        ruleType = RewardRuleType.CLUSTER_COMPLETION,
        amount = ClusterCompletionRewards.creditsFor(cluster),
        label = cluster.clusterType.displayName,
    )
}
