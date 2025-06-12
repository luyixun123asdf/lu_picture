package com.lu.lupicturebackend.code;

import java.util.HashMap;

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

    // 最长斐波那契数列的长度

    public static int lenLongestFibSubSeq(int[] arr) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < arr.length; i++) {
            map.put(arr[i], i);
        }
        int[][] dp = new int[arr.length][arr.length];
        int result = 2;
        for (int i = 1; i < arr.length; i++)
            for (int j = 0; j < i; j++) {
                Integer k = map.getOrDefault(arr[i] - arr[j], -1);
                dp[i][j] = k >= 0 && k < j ? dp[j][k] + 1 : 2;
                result = Math.max(result, dp[i][j]);
            }
        return result > 2 ? result : 0;

    }


    public static void main(String[] args) {
//        int[][] costs = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};  // 刷房子
//        System.out.println(MyCost(costs));

        int[] arr = {1, 2, 3, 4, 5, 6, 7, 8};
        System.out.println(lenLongestFibSubSeq(arr));
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
        return Math.min(dp[0][last], Math.min(dp[1][last], dp[2][last]));


    }
}
