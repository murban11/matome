# Matome

A program that generates linguistic summarizations of a body performance
dataset[^1].

Both, single-subject and multiple-subject summaries can be generated of the
forms from I to IV. The quality of the summaries is determined by the
$T_1$ - $T_{11}$ measures[^2].

## Quick start

* Download the dataset from
<https://www.kaggle.com/datasets/kukuroo3/body-performance-data>
and put it in the project's top-level directory as `bodyPerformance.csv`;
* Execute the following commands:
    ```console
    $ python preprocess_dataset.py
    $ mvn javafx:run
    ```

## Running in CLI mode

> [!WARNING]
> Execution of the program in CLI mode with the default `config.json` may
> take significant amount of time. You may want to reduce the number of
> quantifiers and qualifiers first in the `config.json` file.

In order to run the program without GUI, the dataset needs to be downloaded
and preprocessed as in the previous step, and the following commands need to
be executed:

```console
$ mvn compile assembly:single
$ java -jar target/matome-<VERSION>-SNAPSHOT-jar-with-dependencies.jar [OPTIONS]
```

The `<VERSION>` part in the last command needs to be replaced by a specific
version, e.g. `1.0-SNAPSHOT`.

The available `OPTIONS` can be obtained by running the last command with the
`--help` option.

## Sample summaries

The following table shows sample summaries generated with Matome:

| **Summary** | **T** | **Form** |
| :--- | :---: | :---: |
| About 2000 people are tall | 0.781 | I |
| About 1/4 people being tall have very strong grip force | 0.914 | II |
| About 3/4 females compared to males are short and have normal grip force | 0.916 | I |
| About half females, compared to males being young, are medium height | 0.981 | II |
| Almost all females being short, compared to males, are underweight (BMI) | 0.995 | III |
| More males than females have moderate broad jump | 0.547 | IV |

## Attribures

Below is a list of attributes used to generate summaries. Most of them come
from the body performance dataset, but some are generated by the
`preprocess_dataset.py` script.

* **age**, expressed by an integer between 21 and 64, inclusivly.
* **gender**, expressed by one of two values: *M*, representing the male gender and *F*, representing the female gender.
* **height** in centimeters, expressed by a real number to one decimal place between 125.0 and 194.0.
* **weight** in kilograms, expressed by a real number to one decimal place between 26.3 and 138.0.
* **BMI** (Body Mas Index) in kilograms per square meter, expressed by a real number to one decimal place between 0.0 and 90.0, generated based on **weight** and **height** from the following formula:<br />$$BMI=\frac{weight}{(0.01\cdot height)^2}$$
* **body fat** (percentage), expressed by a real number to one decimal place between 3.0 and 78.4.
* **modified body fat**, generated based on the **body fat** and **gender** attributes. If the value of the **gender** attribute of a record is *M*, the value of this attribute is the same as the value of the **body fat** attribute. Otherwise, the value of this attribute is the value of the **body fat** attribute minus 8.2. This is because women have an average of 8.2 more body fat percentage than men[^3].
* **diastolic**, i.e. blood pressure in the diastolic phase of the cardiac cycle, expressed by an integer between 0 and 156.
* **systolic**, i.e. blood pressure in the systolic phase of the cardiac cycle, expressed by an integer between 0 and 201.
* **grip force** in kilograms measured with a handheld dynanometer, expressed as a real number to one decimal place between 0.0 and 70.5.
* **sit and bend forward**, i.e. the longest distance in centimeters that the tips of the fingers travel horizontally during forward bending in the sitting position, expressed by a real number to one decimal place between -25.0 and 42.0. A negative value of this attribute means that the person was not able to assume an upright sitting position (i.e., at 90°) and the given value is the path needed to assume this position.
* **sit-ups count**, i.e. the number of sit-ups performed in 2 minutes, expressed by an integer between 0 and 80.
* **broad jump** length in centimeters, expressed by an integer between 0 and 303.

## Default configuration

<details>
<summary>Linguistic variables</summary>

* **age**<br />
  Domain: [20, 70]<br />
  $`
  \begin{align}
    \mathrm{age}_{\mathrm{YOUNG}}(x) &= \begin{cases}
      1 & \text{ for } x < 30 \\
      \frac{50-x}{20} & \text{ for } x \in [30, 50)
    \end{cases} \\
    \mathrm{age}_{\mathrm{AVERAGE}}(x) &= \exp\left(-\frac{1}{2}\left(\frac{x-36.8}{2}\right)^2\right) \\
    \mathrm{age}_{\mathrm{MIDDLE-AGED}}(x) &= \begin{cases}
      \frac{x-35}{10} & \text{ for } x \in [35, 45) \\
      1 & \text{ for } x \in [45, 55) \\
      \frac{65-x}{10} & \text{ for } x \in [55, 65) \\
    \end{cases} \\
    \mathrm{age}_{\mathrm{OLD}}(x) &= \begin{cases}
      \frac{x-50}{20} & \text{ for } x \in [50, 70) \\
      1 & \text{ for } x \ge 70 \\
    \end{cases}
  \end{align}
  `$
  ![age](https://github.com/user-attachments/assets/2faf04f3-79e3-48d9-9bb9-299931783c69)
* **BMI**<br />
  Domain: [0, 90]<br />
  $`
  \begin{align}
    \mathrm{BMI}_{\mathrm{UNDERWEIGHT}}(x) &= \begin{cases}
      1 & \text{ for } x < 16 \\
      \frac{21-x}{5} & \text{ for } x \in [16, 21) \\
    \end{cases} \\
    \mathrm{BMI}_{\mathrm{NORMAL-WEIGHT}}(x) &= \begin{cases}
      \frac{x-16.5}{4} & \text{ for } x \in [16.5, 20.5) \\
      1 & \text{ for } x \in [20.5, 23) \\
      \frac{27-x}{4} & \text{ for } x \in [23, 27) \\
    \end{cases} \\
    \mathrm{BMI}_{\mathrm{OVERWEIGHT}}(x) &= \begin{cases}
      \frac{x-23}{4} & \text{ for } x \in [23, 27) \\
      1 & \text{ for } x \in [27, 28) \\
      \frac{32-x}{4} & \text{ for } x \in [28, 32) \\
    \end{cases} \\
    \mathrm{BMI}_{\mathrm{OBESE}}(x) &= \begin{cases}
      \frac{x-28}{4} & \text{ for } x \in [28, 32) \\
      1 & \text{ for } x \ge 32 \\
    \end{cases}
  \end{align}
  `$
  ![BMI](https://github.com/user-attachments/assets/0f75306a-d89a-4410-b297-4ce991c7b359)
* **modified body fat**<br />
  Domain: [0, 80]<br />
  $`
  \begin{align}
    \textrm{modified-body-fat}_{\mathrm{LOW}}(x) &= \begin{cases}
      1 & \text{ for } x < 4 \\
      \frac{20-x}{16} & \text{ for } x \in [4, 20) \\
    \end{cases} \\
    \textrm{modified-body-fat}_{\mathrm{IDEAL}}(x) &= \begin{cases}
      \frac{x+2}{16} & \text{ for } x \in [-2, 14) \\
      1 & \text{ for } x \in [14, 16) \\
      \frac{26-x}{10} & \text{ for } x \in [16, 26) \\
    \end{cases} \\
    \textrm{modified-body-fat}_{\mathrm{AVERAGE}}(x) &= \begin{cases}
      \frac{x-12}{8} & \text{ for } x \in [12, 20) \\
      1 & \text{ for } x \in [20, 22) \\
      \frac{34-x}{12} & \text{ for } x \in [22, 34) \\
    \end{cases} \\
    \textrm{modified-body-fat}_{\mathrm{HIGH}}(x) &= \begin{cases}
      \frac{x-16}{8} & \text{ for } x \in [16, 24) \\
      1 & \text{ for } x \ge 24 \\
    \end{cases}
  \end{align}
  `$
  ![modified body fat](https://github.com/user-attachments/assets/ea398af0-5ec3-43a8-a898-d70ef38b553f)
* **broad jump**<br />
  Domain: [0, 310]<br />
  $`
  \begin{align}
    \textrm{broad-jump}_{\textrm{SHORT}}(x) &= \begin{cases}
      1 & \text{ for } x < 125 \\
      \frac{175-x}{50} & \text{ for } x \in [125, 175) \\
    \end{cases} \\
    \textrm{borad-jump}_{\textrm{MODERATE}}(x) &= \begin{cases}
      \frac{x-125}{50} & \text{ for } x \in [125, 175) \\
      1 & \text{ for } x \in [175, 225) \\
      \frac{275-x}{50} & \text{ for } x \in [225, 275) \\
    \end{cases} \\
    \textrm{broad-jump}_{\textrm{LONG}}(x) &= \begin{cases}
      \frac{x-225}{85} & \text{ for } x \in [225, 310) \\
      1 & \text{ for } x \ge 310 \\
    \end{cases}
  \end{align}
  `$
  ![broad jump](https://github.com/user-attachments/assets/658573e9-f9ea-407b-ae77-33f7c8b336ed)
* **diastolic**<br />
  Domain: [0, 160]<br />
  $`
  \begin{align}
    \mathrm{diastolic}_{\mathrm{HYPOTENSION}}(x) &= \begin{cases}
      1 & \text{ for } x < 50 \\
      \frac{70-x}{20} & \text{ for } x \in [50, 70) \\
    \end{cases} \\
    \mathrm{diastolic}_{\mathrm{NORMAL}}(x) &= \begin{cases}
      \frac{x-50}{20} & \text{ for } x \in [50, 70) \\
      1 & \text{ for } x \in [70, 80) \\
      \frac{100-x}{20} & \text{ for } x \in [80, 100) \\
    \end{cases} \\
    \mathrm{diastolic}_{\mathrm{HYPERTENSION}}(x) &= \begin{cases}
      \frac{x-80}{20} & \text{ for } x \in [80, 100) \\
      1 & \text{ for } x \ge 100 \\
    \end{cases}
  \end{align}
  `$
  ![diastolic](https://github.com/user-attachments/assets/4e58e77f-c542-4074-90c1-d7d3e4c6e923)
* **systolic**<br />
  Domain: [0, 210]<br />
  $`
  \begin{align}
    \mathrm{systolic}_{\mathrm{HYPOTENSION}}(x) &= \begin{cases}
      1 & \text{ for } x < 70 \\
      \frac{110-x}{40} & \text{ for } x \in [70, 110) \\
    \end{cases} \\
    \mathrm{systolic}_{\mathrm{NORMAL}}(x) &= \begin{cases}
      \frac{x-70}{40} & \text{ for } x \in [70, 110) \\
      1 & \text{ for } x \in [110, 120) \\
      \frac{160-x}{40} & \text{ for } x \in [120, 160) \\
    \end{cases} \\
    \mathrm{systolic}_{\mathrm{HYPERTENSION}}(x) &= \begin{cases}
      \frac{x-120}{40} & \text{ for } x \in [120, 160) \\
      1 & \text{ for } x \ge 160 \\
    \end{cases}
  \end{align}
  `$
  ![systolic](https://github.com/user-attachments/assets/5d911ca9-e9c4-4400-8ab5-a6febe9ac3bb)
* **grip force**<br />
  Domain: [0, 80]<br />
  $`
  \begin{align}
    \textrm{grip-force}_{\textrm{VERY-WEAK}}(x) &= \begin{cases}
      1 & \text{ for } x < 0 \\
      \frac{16-x}{16} & \text{ for } x \in [0, 16) \\
    \end{cases} \\
    \textrm{grip-force}_{\textrm{WEAK}}(x) &= \begin{cases}
      \frac{x-16}{4} & \text{ for } x \in [16, 20) \\
      1 & \text{ for } x \in [20, 30) \\
      \frac{34-x}{4} & \text{ for } x \in [30, 34) \\
    \end{cases} \\
    \textrm{grip-force}_{\textrm{NORMAL}}(x) &= \begin{cases}
      \frac{x-30}{4} & \text{ for } x \in [30, 34) \\
      \frac{38-x}{4} & \text{ for } x \in [34, 38) \\
    \end{cases} \\
    \textrm{grip-force}_{\textrm{STRONG}}(x) &= \begin{cases}
      \frac{x-34}{4} & \text{ for } x \in [34, 38) \\
      1 & \text{ for } x \in [38, 50) \\
      \frac{54-x}{4} & \text{ for } x \in [50, 54) \\
    \end{cases} \\
    \textrm{grip-force}_{\textrm{VERY-STRONG}}(x) &= \begin{cases}
      \frac{x-50}{4} & \text{ for } x \in [50, 54) \\
      1 & \text{ for } x \ge 54 \\
    \end{cases}
  \end{align}
  `$
  ![grip force](https://github.com/user-attachments/assets/c563515c-3aef-4c88-a5b0-09c29ba4b16f)
* **height**<br />
  Domain: [120, 200]<br />
  $`
  \begin{align}
    \mathrm{height}_{\mathrm{SHORT}}(x) &= \begin{cases}
      1 & \text{ for } x < 150 \\
      \frac{170-x}{20} & \text{ for } x \in [150, 170) \\
    \end{cases} \\
    \mathrm{height}_{\mathrm{MEDIUM-HEIGHT}}(x) &= \begin{cases}
      \frac{x-150}{20} & \text{ for } x \in [150, 170) \\
      \frac{190-x}{20} & \text{ for } x \in [170, 190) \\
    \end{cases} \\
    \mathrm{height}_{\mathrm{TALL}}(x) &= \begin{cases}
      \frac{x-170}{190-170} & \text{ for } x \in [170, 190) \\
      1 & \text{ for } x \ge 190 \\
    \end{cases}
  \end{align}
  `$
  ![height](https://github.com/user-attachments/assets/938978e8-c70a-44ab-97d6-c44afdba03dd)
* **sit and bend forward**<br />
  Domain: [-25, 220]<br />
  $`
  \begin{align}
    \textrm{sit-and-bend-forward}_{\textrm{MINIMAL}}(x) &= \begin{cases}
      1 & \text{ for } x < 5 \\
      \frac{15-x}{10} & \text{ for } x \in [5, 15) \\
    \end{cases} \\
    \textrm{sit-and-bend-forward}_{\textrm{MODERATE}}(x) &= \begin{cases}
      \frac{x-5}{10} & \text{ for } x \in [5, 15) \\
      \frac{25-x}{10} & \text{ for } x \in [15, 25) \\
    \end{cases} \\
    \textrm{sit-and-bend-forward}_{\textrm{ADVANCED}}(x) &= \begin{cases}
      \frac{x-15}{10} & \text{ for } x \in [15, 25) \\
      1 & \text{ for } x \in [25, 30) \\
      \frac{40-x}{10} & \text{ for } x \in [30, 40) \\
    \end{cases} \\
    \textrm{sit-and-bend-forward}_{\textrm{SUPERHUMAN}}(x) &= \begin{cases}
      \frac{x-30}{50} & \text{ for } x \in [30, 80) \\
      1 & \text{ for } x \ge 80 \\
    \end{cases}
  \end{align}
  `$
  ![sit and bend forward](https://github.com/user-attachments/assets/88696a89-c33b-47d5-b06c-0162d3287b3f)
* **sit-ups count**<br />
  Domain: [0, 80]<br />
  $`
  \begin{align}
    \textrm{sit-ups-count}_{\textrm{BEGINNER}}(x) &= \begin{cases}
      1 & \text{ for } x < 10 \\
      \frac{30-x}{10} & \text{ for } x \in [10, 30) \\
    \end{cases} \\
    \textrm{sit-ups-count}_{\textrm{INTERMEDIATE}}(x) &= \begin{cases}
      \frac{x-10}{20} & \text{ for } x \in [10, 30) \\
      \frac{50-x}{20} & \text{ for } x \in [30, 50) \\
    \end{cases} \\
    \textrm{sit-ups-count}_{\textrm{ADVANCED}}(x) &= \begin{cases}
      \frac{x-30}{50} & \text{ for } x \in [30, 80) \\
      1 & \text{ for } x \ge 80 \\
    \end{cases}
  \end{align}
  `$
  ![sit ups count](https://github.com/user-attachments/assets/de2cb647-ab36-4743-bfcc-33136f108831)

</details>

<details>
<summary>Quantifiers</summary>

* **Relative quantifiers**<br />
  $`
  \begin{align}
    \mu_{\textrm{ALMOST-NONE}}(x) &= \exp\left(-\frac{1}{2}\left(\frac{x-0}{0.1}\right)^2\right) \\
    \mu_{\textrm{ABOUT 1/4}}(x) &= \exp\left(-\frac{1}{2}\left(\frac{x-0.25}{0.1}\right)^2\right) \\
    \mu_{\textrm{ABOUT-HALF}}(x) &= \exp\left(-\frac{1}{2}\left(\frac{x-0.5}{0.1}\right)^2\right) \\
    \mu_{\textrm{ABOUT 3/4}}(x) &= \exp\left(-\frac{1}{2}\left(\frac{x-0.75}{0.1}\right)^2\right) \\
    \mu_{\textrm{ALMOST-ALL}}(x) &= \exp\left(-\frac{1}{2}\left(\frac{x-1}{0.1}\right)^2\right)
  \end{align}
  `$
  ![relative quantifiers](https://github.com/user-attachments/assets/2b7bfe9b-57c1-4112-a3a5-ee71ffbb2b44)
* **Absolute quantifiers**<br />
  $`
  \begin{align}
    \mu_{\textrm{LESS-THAN-1000}}(x) &= \begin{cases}
      1 & \text{ for } x < 500 \\
      \frac{1000-x}{500} & \text{ for } x \in [500, 1000) \\
    \end{cases} \\
    \mu_{\textrm{ABOUT-2000}}(x) &= \exp\left(-\frac{1}{2}\left(\frac{x-2000}{2000}\right)^2\right) \\
    \mu_{\textrm{BETWEEN-4000-AND-6000}}(x) &= \begin{cases}
      \frac{x-4000}{500} & \text{ for } x \in [4000, 4500) \\
      1 & \text{ for } x \in [4500, 5500) \\
      \frac{6000-x}{500} & \text{ for } x \in [5500, 6000) \\
    \end{cases} \\
    \mu_{\textrm{ABOUT-8000}}(x) &= \exp\left(-\frac{1}{2}\left(\frac{x-8000}{2000}\right)^2\right) \\
    \mu_{\textrm{MORE-THAN-10000}}(x) &= \begin{cases}
      \frac{x-10000}{500} & \text{ for } x \in [10000, 10500) \\
      1 & \text{ for } x \ge 10500 \\
    \end{cases}
  \end{align}
  `$
  ![absolute quantifiers](https://github.com/user-attachments/assets/5e0664bb-cdd1-4bed-b637-a9a4df7fe5f3)

</details>

## LICENSE

[MIT](https://github.com/murban11/matome/blob/main/LICENSE)

[^1]: Korea Sports Promotion Foundation, Body Performance Data, Kaggle. URL: <https://www.kaggle.com/datasets/kukuroo3/body-performance-data>.
[^2]: A. Niewiadomski, Methods for the Linguistic Summarization of Data: Applications of Fuzzy Sets and Their Extensions, Akademicka Oficyna Wydawnicza EXIT, Warszawa 2008.
[^3]: Cureton, K. J., Hensley, L. D., & Tiburzi, A. (1979). Body Fatness and Performance Differences between Men and Women. Research Quarterly. American Alliance for Health, Physical Education, Recreation and Dance. <https://doi.org/10.1080/00345377.1979.10615619>.
