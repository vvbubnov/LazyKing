import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/*
 * В одной далекой стране правил крайне сумасбродный король, который больше всего на свете любил власть.
 * Ему подчинялось множество людей, но вот незадача, у его подчиненных тоже были свои слуги.
 * Король обезумел от мысли, что какой-нибудь дворянин или даже зажиточный холоп может иметь больше слуг, чем он сам.
 * И приказал всем людям на бумаге через запятую написать свое имя и имена своих прямых подчиненных.
 *
 * По результатам опроса король получил огромный список из имен (see "pollResults")
 *
 * У короля разболелась голова. Что с этими данными делать, король не знал и делегировал задачу невезучему слуге.

 * Помогите слуге правильно составить иерархию и подготовить  отчет для короля следующим образом:
 *
 * король
 *     дворянин Кузькин
 *         управляющий Семен Семеныч
 *             крестьянин Федя
 *             доярка Нюра
 *         жена Кузькина
 *         ...
 *     секретарь короля
 *         зажиточный холоп
 *         ...
 *     ...
 *
 * Помните:
 *  1. Те, у кого нет подчиненных, просто написали свое имя.
 *  2. Те, кого никто не указал как слугу, подчиняются напрямую королю (ну, пускай бедный король так думает).
 *  3. Итоговый список должен быть отсортирован в алфавитном порядке на каждом уровне иерархии.
 *
 * Ответ присылайте ссылкой на опубликованный приватный Gist.
 * -------------------------
 */

public class LazyKing {
    private static List<String> pollResults = List.of(
            "служанка Аня",
            "управляющий Семен Семеныч: крестьянин Федя, доярка Нюра",
            "дворянин Кузькин: управляющий Семен Семеныч, жена Кузькина, экономка Лидия Федоровна",
            "экономка Лидия Федоровна: дворник Гена, служанка Аня",
            "доярка Нюра",
            "кот Василий: человеческая особь Катя",
            "дворник Гена: посыльный Тошка",
            "киллер Гена",
            "зажиточный холоп: крестьянка Таня",
            "секретарь короля: зажиточный холоп, шпион Т",
            "шпион Т: кучер Д",
            "посыльный Тошка: кот Василий",
            "аристократ Клаус",
            "просветленный Антон"
    );

    public static void main(String... args) {
        UnluckyVassal unluckyVassal = new UnluckyVassal();

        unluckyVassal.printReportForKing(pollResults);
    }
}

class UnluckyVassal {

    // количество поклонов, которые должен сделать вассал своему сюзерену
    // если по нормальному - это отступ
    static final String TAB = "    ";

    // здесь будут зарегестрированы все наши слуги
    List<Vassal> vassalList = new ArrayList<>();

    public void printReportForKing(List<String> pollResults) {

        // бежим построчно по исходному тексту
        for (String note : pollResults) {

            // получаем массив стрингов через регулярочку, разделители ": " и ", "
            // это ни что иное как имена действующих лиц
            String[] names = note.split(": |, ");

            // 1)   у данного массива не может не быть первого элемента
            //      проверяем изветрен ли он нам, если нет - регистрируем
            Optional<Vassal> senior = getExistedVassalByName(names[0]);
            if (senior.isEmpty()) {
                Vassal newSenior = new Vassal(names[0]);
                vassalList.add(newSenior);
                senior = Optional.of(newSenior);
            }

            // 2)   перебираем остальные элементы - это слуги первого элемента
            //      добавляем их в список вассалов первого элемента,
            //      выставляем флажок на false - эти вассалы теперь не являются прямыми вассалами короля
            for (int i = 1; i < names.length; i++) {
                if (senior.get().vassals == null ){
                    senior.get().vassals = new ArrayList<>();
                }
                Optional<Vassal> existedVassal = getExistedVassalByName(names[i]);
                if (existedVassal.isEmpty()) {
                    Vassal newVassal = new Vassal(names[i]);
                    vassalList.add(newVassal);
                    senior.get().vassals.add(newVassal);
                    newVassal.setKingsVassal(false);
                } else {
                    senior.get().vassals.add(existedVassal.get());
                    existedVassal.get().setKingsVassal(false);
                }
            }
        }

        // сортируем в алфавитном порядке основной лист вассалов, а также
        // листы привязанные к каждому объекту класса Vassal
        vassalList.sort(Comparator.comparing(Vassal::getName));
        for (Vassal vassal : vassalList) {
            if (vassal.vassals != null) {
                vassal.vassals.sort(Comparator.comparing(Vassal::getName));
            }
        }

        // объявляем короля
        System.out.println("король");

        // просим вассалов рассказать о себе и своих подчинённых, но только тех (!) у кого флажок true
        // т.о. мы не увидем дублированной информации
        for (Vassal vassal : vassalList) {
            if (vassal.isKingsVassal()) {
                System.out.print(vassal.selfReport(TAB));
            }
        }
    }

    /**
     * вспомогательный метод который ходит в список зарегестрированных вассалов и ищет их по имени
     * @return Optional<Vassal>
     */
    Optional<Vassal> getExistedVassalByName(String name) {
        return vassalList.stream()
                            .filter(v -> v.name.equals(name))
                            .findFirst();
    }
}

/**
 * Основной класс, описывающий вассала. Содержит его имя, список его вассалов, а также
 * флажок, который говорит нам: является ли подданый прямым вассалом кораля?
 * По дефолту true (изначально каждый новый вассал - королевский вассал)
 * False если возникает ситуация когда у текущего вассала появляется другой хозяин
 */
class Vassal {

    String name;
    List<Vassal> vassals;
    boolean KingsVassal = true;

    Vassal(String name) {
        this.name = name;
    }

    // этот геттер нужен чтобы заработал модный компаратор
    public String getName() {
        return name;
    }

    public boolean isKingsVassal() {
        return KingsVassal;
    }

    public void setKingsVassal(boolean kingsVassal) {
        this.KingsVassal = kingsVassal;
    }

    // метод, с помощью которого вассал может рассказать о себе и своих подчинённых
    String selfReport(String tab) {

        StringBuilder result = new StringBuilder();

        // докладываем о себе
        result.append(tab).append(name).append("\n");

        // если список вассалов не пустой -> просим каждого подчинённого рассказать о себе
        // вроде рекурсивненько получилось...
        if (vassals != null) {
            for (Vassal vassal : vassals) {
                result.append(vassal.selfReport(tab + UnluckyVassal.TAB));
            }
        }
        return result.toString();
    }
}