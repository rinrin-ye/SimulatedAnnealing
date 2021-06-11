# SimulatedAnnealing

------

###### 모의 담금질 기법을 이용한 parameter estimation



## 1. 모의 담금질 기법이란

모의 담금질(Simulated Annealing) 기법은 높은 온도에서 액체 상태인 물질이 온도가 점차 낮아지면서 결정체로 변하는 과정을 모방한 해 탐색 알고리즘이다. 융용 상태에서는 물질의 분자가 자유로이 움직이는 것을 모방하여 해를 탐색하는 과정도 특정한 패턴 없이 이루어지고, 온도가 낮아지면 분자의 움직임도 점점 줄어들어 결정체가 되는 것을 모방하여 해 탐색 과정도 점점 더 규칙적인 방식으로 이루어진다.

그러나 모의 담금질 기법은 항상 전역 최적해를 찾아준다는 보장이 없다. 그리고 하나의 초기 해로부터 탐색이 진행된다는 특징을 가진다. 

모의 담금질 기법의 기본적인 알고리즘은 다음과 같다.

------

**SA(Problem p, double t, double a, double x0)**

- 입력 : 문제 p, 초기온도 t, 냉각율 a, 초기 후보해 x0
- 출력 : 최적해 s

s = 임의의 후보해 ( = 초기 후보해 x0)

T = 초기 온도 t

repeat

for i = 1 to kT {  // kT는 T에서의 for-루프 반복 횟수

​	s' = s의 이웃해 중에서 랜덤하게 하나의 해

​	d = (s'의 값) - (s의 값)

​	if (이웃해인 s'가 더 우수한 경우)

​		s ← s'

​	else       // s'가 s보다 우수하지 않은 경우

​		q ← (0,1) 사이에서 랜덤하게 선택한 수

​		if ( q < p ) s ← s'  // p는 자유롭게 탐색할 확률로 보통 e^(-d/T)

 }

T ← aT // 1보다 작은 상수 a 를 T에 곱하여 새로운 T를 계산 (a=냉각률)

until (종료 조건이 만족될 때까지)

return s



## 2. 코드 설명

1. Interface Problem 구현

   문제의 적합도를 판별할 fit 메소드와 이웃해와 현재의 후보해의 적합도를 비교하는 isNeighborBetter 메소드

```java
public interface Problem {
    double fit(double x);

    boolean isNeighborBetter(double f0, double f1);
}
```



2. class SimulatedAnnealing 구현

```java
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing {
    private int niter;
    public ArrayList<Double> hist; //최적해 도달까지 변화한 적합도 값 저장

    public SimulatedAnnealing(int niter) {
        this.niter = niter;
        hist = new ArrayList<>();
    }

    public double solve(Problem p, double t, double a, double lower, double upper) {
        Random r = new Random();
        double x0 = r.nextDouble() * (upper - lower) + lower; //
        return solve(p, t, a, x0, lower, upper);
    } //solve메소드에 초기 후보해를 인수로 전달해 주지 않을 경우 임의로 지정한 후 초기 후보해를 넣어 오버로딩한 solve메소드 호출

    public double solve(Problem p, double t, double a, double x0, double lower, double upper) {
        Random r = new Random();
        double f0 = p.fit(x0);
        hist.add(f0);
        if (a >= 1) {
            a = 0.99;
        } //T는 갈수록 0에 가까워져야하므로 냉각률이 1이상일 경우 0.99로 설정해준다.
        for (int i = 0; i < niter; i++) {
            int kt = (int) t; //온도 t에서의 for-루프 반복 횟수 kt(이때, t가 낮아질수록 for-루프 반복 횟수인 kt도 작아진다.)
            for (int j = 0; j < kt; j++) {
                double x1 = r.nextDouble() * (upper - lower) + lower; //이웃해 선택
                double f1 = p.fit(x1);

                if (p.isNeighborBetter(f0, f1)) {
                    x0 = x1;
                    f0 = f1; //이웃해의 적합도가 더 높을 경우 이웃해를 후보해로 선택
                    hist.add(f0);
                } else { //현재 후보해의 적합도가 이웃해의 적합도가 더 높은 경우
                    double d = Math.abs(f1 - f0);
                    double p0 = Math.exp(-d / t);
                    if (r.nextDouble() < p0) { //좋지 않은 이웃해를 선택할 확률인 p0는 t(온도)에 반비례하고 d(현재 후보해와 이웃해의 차이)와 비례한다.
                        x0 = x1;
                        f0 = f1; //현재 후보해의 적합도가 더 높더라도 자유롭게 탐색할 확률에 따라 좋지 않은 이웃해를 선택한다.
                        hist.add(f0);
                    }
                }
            }
            t *= a;
        }
        return x0;
    }
}
```



3. main

- 3-4차 함수를 찾기 위한 main 구현

이때에는 problem의 적합도를 함수의 값으로 판별하여 최대값이나 최솟값을 구한다.

```java
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
            } //최대값을 구하기 위해 큰값일 경우 적합도가 더 높다(이웃해가 더 좋다)고 판단
        };
        double x = sa.solve(p, 100, 0.99, 0, -15, 15);
        System.out.println("f(x) : x^3 - 15x^2 - 72x + 110");
        System.out.println("최적점 : " + x);
        System.out.println("최적해 : " + p.fit(x));
        System.out.println(sa.hist); //최적해를 찾기까지의 과정
    }
}
```

- 선형 모델 데이터의 가장 적합한 파라미터를 찾기 위한 main 구현

이때의 problem의 적합도는 원래의 데이터 값과 임의의 값을 파라미터에 대입한 값을 이용한 선형 모델의 값의 차이의 제곱을 데이터 수로 나눈 것으로 판별한다.

즉, 원래의 데이터 값과 차이를 파라미터 값 측에서 바라보면 그 차이의 값도 파라미터 값에 따른 함수로 표현될 수 있다. 그래서 그 차이가 최소가 되는 최적점을 위에서 구현한 모의담금질 기법을 이용하여 구해주는 것이다.

```java
public class Main {
    public static void main(String[] args) {
        SimulatedAnnealing sa = new SimulatedAnnealing(1000);
        int[][] data = {{1, 10119}, {2, 11642}, {3, 9437}, {4, 9529}, {5, 45199}, {6, 11367}, {7, 14365}, {8, 24906}, {9, 32231}, {10, 31935}, {11, 33510}};

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
```



####  전체 자바 코드는 [여기](https://github.com/rinrin-ye/SimulatedAnnealing/tree/main/src/com/company)에서 확인가능하다.



## 3. 코드 결과

1. 3~4차함수의 전역 최적점을 찾을 수 있는 모의담금질 기법을 구현한 알고리즘으로

<img src="https://user-images.githubusercontent.com/80517298/121670390-57407e80-cae8-11eb-8d18-0b86ef7f51e7.png" alt="3차 함수 그래프" width="480" height="400" />

위 그림과 같은 3차 함수 f(x) : x^3 - 15x^2 - 72x + 110의 최적점(구간 [-15, 15]에서의 최대값)을 찾아보았다. 실제의 최적점은 최대값 f(x)=186 를 가지는  x=-2 이다.

<img src="https://user-images.githubusercontent.com/80517298/121670131-12b4e300-cae8-11eb-872a-20dafbdd8ebe.jpg" alt="코드결과 1" width="700" height="200" />

위의 결과값을 보면 알 수 있듯이 실제의 최적점과 거의 근접한 결과를 얻을 수 있다.



2.  2015년 월별(1월~11월) 전라북도 미륵사지 관광객 현황을 이용하여  curve fitting을 위한 선형 모델을 **y=ax+b**(x=월, y=월별 개인 관광객 수)로 설정하고 위의 모의담금질 기법을 이용하여 적합한 파라미터 값 a, b를 구해보려고 한다. 

<img src="https://user-images.githubusercontent.com/80517298/121669451-48a59780-cae7-11eb-8f57-5508e58d96ef.jpg" alt="미륵사지 관광객 현황" width="150" height="300" />

[^]: 해당 데이터는 [공공데이터포털 (data.go.kr)](https://www.data.go.kr/data/15045413/fileData.do) 에서 가져왔다.



<img src="https://user-images.githubusercontent.com/80517298/121670124-10528900-cae8-11eb-82e2-9365966e7ea5.jpg" alt="코드결과 2" width="450" height="250" />

위의 코드 결과는 위에서 구현한 모의담금질 기법을 이용하여 본래의 데이터와 선형 모델 간의 차이가 적은 파라미터를 구해준 것이다.



<img src="https://user-images.githubusercontent.com/80517298/121670041-ffa21300-cae7-11eb-9b95-94ea4945cd73.png" alt="적합한 파라미터" width="450" height="400" />

이는 위에서 구한 파라미터 값을 선형 모델 y=ax+b에 대입한 그래프를 원래의 데이터 값들과 함께 표시한 그래프이다.





## 4. 성능 분석

- niter의 값과 성능

niter(최적해를 구하기 위한 전체 반복 루프 횟수)에 큰 값을 줄수록 반복횟수가 늘어나 소요시간은 증가하지만 최적해에 더 근접한 값을 얻을 수 있었다.



- 이웃해 선택과 성능

이웃해를 랜덤으로 설정하여 나타낸 경우, 온도와 관계없이 후보해를 선택하여 온도가 낮아져도 후보해의 변화율이 크다.

<img src="https://user-images.githubusercontent.com/80517298/121696608-3176a200-cb07-11eb-9339-e809a94c0493.jpg" alt="이웃해 - 랜덤" width="450" height="200" />

 

그래서 온도가 낮아질수록 후보해의 변화율이 적고 최적해값으로 수렴하도록 만들기 위해서 

```java
int kt = (int) 50*T/t; //온도 t에서의 for-루프 반복 횟수 kt
```

온도가 낮아질수록 kt의 값이 증가하도록 만들고 *(T는 초기 온도이며, 위는 임의의 예시 코드임)*

```java
double x1 = ? ; //이웃해 선택
```

이웃해의 값도 온도가 낮아질수록 최적해에 근접하는 이웃해를 선택하도록 코드를 수정하려고 했으나 이웃해 선택을 하는 방식을 코드로 구현하는 것이 어려워 완성하지 못하였다.

```java
double x1 = [0, T/t] +/* ?;
```

위와 같이 t/T 값에 따라 이웃해의 변화율을 좁혀준다면 수렴하는 그래프를 만들 수 있을 것이다. *(T는 초기 온도이고 t는 현재 루프가 돌아갈 때의 온도이다. 즉, t는 값이 점점 작아지므로 t/T의 값도 1에서 점점 작아질 것이다.)* 그러나 이때 t/T를 이용하여 t/T에 어떤 값을 곱하거나 x0에 값을 더해주는 방법 등으로 t/T에 어떠한 값을 더하거나 곱해주어 이웃해를 설정해주려 했으나 어떻게 설정 해야할지를 정하기가 어려워서 구현을 하지는 못했다. 
