package com.company;

public class Main {
    public static void main(String[] args) {
        SimulatedAnnealing sa = new SimulatedAnnealing(1000);
        Problem p = new Problem() {
            @Override
            public double fit(double x) {
                return x * x * x - 15 * x * x - 72 * x + 110;
                // x=-2 , f(x)=186 - 최대값
            }

            @Override
            public boolean isNeighborBetter(double f0, double f1) {
                return f0 < f1;
            } //큰값선택
        };
        double x = sa.solve(p, 100, 0.99, 0, -15, 15);
        System.out.println("f(x) : x^3 - 15x^2 - 72x + 110");
        System.out.println("최적점 : " + x);
        System.out.println("최적해 : " + p.fit(x));
        System.out.println(sa.hist);


        int[][] data = {{1, 10119}, {2, 11642}, {3, 9437}, {4, 9529}, {5, 45199}, {6, 11367},
                {7, 14365}, {8, 24906}, {9, 32231}, {10, 31935}, {11, 33510}};

        Problem p1 = new Problem() {
            @Override
            public double fit(double x) {
                double sum = 0;
                for (int i = 0; i < data.length; i++) {
                    int xv = data[i][0];
                    sum += Math.pow(x * xv - data[i][1], 2);
                }
                return sum / data.length;
            }

            @Override
            public boolean isNeighborBetter(double f0, double f1) {
                return f0 > f1;
            } //작은값선택
        };
        double a = sa.solve(p1, 100, 0.99, 2000, 2000, 5000);
        System.out.println("\ny=ax 선형 모델에 가장 적합한 파라미터");
        System.out.println("a : " + a);
        System.out.println("데이터와의 차이값 : " + p1.fit(a));

        Problem p2 = new Problem() {
            @Override
            public double fit(double x) {
                double sum = 0;
                for (int i = 0; i < data.length; i++) {
                    int xv = data[i][0];
                    sum += Math.pow((a * xv + x) - data[i][1], 2);
                }
                return sum / data.length;
            }

            @Override
            public boolean isNeighborBetter(double f0, double f1) {
                return f0 > f1;
            } //작은값선택
        };
        double b = sa.solve(p2, 100, 0.99, 2000, 2000, 5000);
        System.out.println("\ny=ax+b 선형 모델에 가장 적합한 파라미터");
        System.out.println("a : " + a);
        System.out.println("b : " + b);
        System.out.println("데이터와의 차이값 : " + p2.fit(b));
    }
}
