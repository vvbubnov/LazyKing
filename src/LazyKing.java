import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
    public void printReportForKing(List<String> pollResults) {

        // здесь будут зарегестрированы все наши слуги
        List<Vassal> vassalList = new ArrayList<>();

        // бежим построчно по исходному тексту
        for (String note : pollResults) {

            // получаем массив стрингов через регулярочку, разделители ": " и ", "
            String[] names = note.split(": |, ");

            // 1) у нас на руках массив с единственным элементом. это говорит о том,
            //      что мы получили вассала у которого нет подчинённых
            if (names.length == 1) {
                // пытаемся получить вассала из списка зарегестрированных
                // если такового нет - создаём нового и регистрируем в листе
                Vassal existedVassal = getExistedVassalByName(vassalList, names[0]);
                if (existedVassal == null) {
                    vassalList.add(new Vassal(names[0]));
                }
            // 2) полученный массив содержит более одного элемента
            } else {
                // 2.1) первый элемент такого массива - хозяин остальных элементов
                Vassal senior = getExistedVassalByName(vassalList, names[0]);
                if (senior == null) {
                    senior = new Vassal(names[0]);
                    vassalList.add(senior);
                }

                // 2.2) перебираем остальные элементы - это слуги первого элемента
                //      добавляем их в список вассалов первого элемента,
                //      выставляем флажок на true - эти вассалы теперь не являются прямыми вассалами короля
                for (int i = 1; i < names.length; i++) {
                    Vassal existedVassal = getExistedVassalByName(vassalList, names[i]);
                    if (existedVassal == null) {
                        Vassal newVassal = new Vassal(names[i]);
                        vassalList.add(newVassal);
                        senior.vassals.add(newVassal);
                        newVassal.setNotKingsVassal(true);
                    } else {
                        senior.vassals.add(existedVassal);
                        existedVassal.setNotKingsVassal(true);
                    }
                }
            }
        }

        // сортируем в алфавитном порядке основной лист вассалов, а также
        // листы привязанные к каждому объекту класса Vassal
        // todo кривовато получилось. производится много лишней работы
        vassalList.sort(Comparator.comparing(Vassal::getName));
        for (Vassal vassal : vassalList) {
            vassal.vassals.sort(Comparator.comparing(Vassal::getName));
        }

        // объявляем короля
        System.out.println("король");

        // просим вассалов рассказать о себе и своих подчинённых,
        // но только тех (!) у кого флажок false
        // т.о. мы не увидем дублированной информации
        for (Vassal vassal : vassalList) {
            if (!vassal.isNotKingsVassal()) {
                System.out.print(vassal.selfReport("    "));
            }
        }
    }

    /**
     * вспомогательный метод который ходит в список зарегестрированных вассалов и ищет их по имени
     * @param vassalList
     * @param name
     * @return объект класса Vassal если нашёл, null - если не нашёл
     *
     * todo по модному надо возвращать Optional<Vassal>
     */
    Vassal getExistedVassalByName(List<Vassal> vassalList, String name) {
        return vassalList.stream()
                .filter(v -> v.name.equals(name))
                .findFirst()
                .orElse(null);
    }
}

/**
 * Основной класс, описывающий наших слуг
 */
class Vassal {


    String name;
    List<Vassal> vassals = new ArrayList<>();

    // поле, которое говорит нам: есть ли у слуги хозяин, который не является королём
    // по дефолту false (изначально каждый новый вассал - королевский вассал)
    // меняется на true если возникает ситуация когда у текущего вассала появляется хозяин
    boolean notKingsVassal = false;

    Vassal(String name) {
        this.name = name;
    }

    // этот геттер нужен чтобы заработал модный компаратор
    public String getName() {
        return name;
    }

    public boolean isNotKingsVassal() {
        return notKingsVassal;
    }

    public void setNotKingsVassal(boolean notKingsVassal) {
        this.notKingsVassal = notKingsVassal;
    }

    // метод, с помощью которого вассал может рассказать о себе и своих подчинённых
    // принимает на впуск отступ todo по хорошему надо сделать так, чтобы он ничего не принимал
    String selfReport(String tab) {

        StringBuilder result = new StringBuilder();

        // докладываем о себе
        result.append(tab).append(name).append("\n");

        // увеличеваем отступ
        String nextTab = tab + "    ";

        // если список вассалов не пустой -> просим каждого подчинённого рассказать о себе
        // вроде рекурсивненько получилось...
        if (!vassals.isEmpty()) {
            for (Vassal vassal : vassals) {
                result.append(vassal.selfReport(nextTab));
            }
        }
        return result.toString();
    }
}