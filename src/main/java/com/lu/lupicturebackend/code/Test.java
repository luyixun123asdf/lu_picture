package com.lu.lupicturebackend.code;

/**
 * 动态规划（刷房子问题）
 */
public class Test {

    public static int minCost(int[][] costs) {
        if (costs.length == 0) {
            return 0;
        }
        int[][] dp = new int[3][2];
        for (int j = 0; j < 3; j++) {
            dp[j][0] = costs[0][j];
        }

        for (int i = 1; i < costs.length; i++) {
            for (int j = 0; j < 3; j++) {
                int prev1 = dp[(j + 2) % 3][(i - 1) % 2];
                int prev2 = dp[(j + 1) % 3][(i - 1) % 2];
                dp[j][i % 2] = Math.min(prev1, prev2) + costs[i][j];
            }
        }
        int last = (costs.length - 1) % 2;
        return Math.min(dp[0][last], Math.min(dp[1][last], dp[2][last]));
    }

    public static void main(String[] args) {
        int[][] costs = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
        System.out.println(MyCost(costs));
    }


    public static int MyCost(int[][] costs1) {
        if (costs1.length == 0) {
            return 0;
        }
        int[][] dp = new int[3][2];
        for (int j = 0; j < 3; j++) {
            dp[j][0] = costs1[0][j];
        }
        for (int i = 1; i < costs1.length; i++) {
            for (int j = 0; j < 3; j++) {
                int pre1 = dp[(j + 1) % 3][(i - 1) % 2];
                int pre2 = dp[(j + 2) % 3][(i - 1) % 2];
                dp[j][i % 2] = Math.min(pre1, pre2) + costs1[i][j];
            }
        }
        int last = (costs1.length - 1) % 2;
        return  Math.min(dp[0][last], Math.min(dp[1][last], dp[2][last]));


    }
}
