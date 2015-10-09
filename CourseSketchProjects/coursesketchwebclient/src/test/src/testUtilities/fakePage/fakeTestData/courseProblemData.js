(function() {

    CourseSketch.fakeProblems = [];
    CourseSketch.fakeBankProblems = [];

    var problem1 = CourseSketch.prutil.SrlProblem();
    var problem2 = CourseSketch.prutil.SrlProblem();
    var problem3 = CourseSketch.prutil.SrlProblem();
    var problem4 = CourseSketch.prutil.SrlProblem();
    var problem5 = CourseSketch.prutil.SrlProblem();
    var problem6 = CourseSketch.prutil.SrlProblem();
    var problem7 = CourseSketch.prutil.SrlProblem();
    var problem8 = CourseSketch.prutil.SrlProblem();
    var problem9 = CourseSketch.prutil.SrlProblem();
    var problem10 = CourseSketch.prutil.SrlProblem();
    var problem11 = CourseSketch.prutil.SrlProblem();
    var problem12 = CourseSketch.prutil.SrlProblem();
    var problem13 = CourseSketch.prutil.SrlProblem();
    var problem14 = CourseSketch.prutil.SrlProblem();

    var bankProblem1 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem2 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem3 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem4 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem5 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem6 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem7 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem8 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem9 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem10 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem11 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem12 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem13 = CourseSketch.prutil.SrlBankProblem();
    var bankProblem14 = CourseSketch.prutil.SrlBankProblem();

    problem1.courseId = "1";
    problem1.assignmentId = "1";
    problem1.id = "1";
    problem1.name = "Test Problem";
    problem1.state = CourseSketch.prutil.State();
    problem1.state.published = true;
    problem1.state.accessible = true;// late
    problem1.state.pastDue = true;
    problem1.state.started = false;
    problem1.state.completed = false;
    problem1.state.graded = true;
    problem1.problemInfo = bankProblem1;
    bankProblem1.id = problem1.id;
    bankProblem1.questionText = "Please write your name." +
        "\left [ – \frac{\hbar^2}{2 m} \frac{\partial^2}{\partial x^2} + V \right ] \Psi = i \hbar \frac{\partial}{\partial t} \Psi";
    CourseSketch.fakeProblems.push(problem1);
    CourseSketch.fakeBankProblems.push(bankProblem1);

    problem2.courseId = "1";
    problem2.assignmentId = "1";
    problem2.id = "2";
    problem2.other = "";
    problem2.gradeWeight = "15";
    problem2.grade = 97;
    problem2.state = CourseSketch.prutil.State();
    problem2.state.published = true;
    problem2.state.accessible = true;//started
    problem2.state.pastDue = false;
    problem2.state.started = true;
    problem2.state.completed = false;
    problem2.state.graded = false;
    problem2.problemInfo = bankProblem2;
    bankProblem2.id = problem2.id;
    bankProblem2.questionText = "Please type your name";
    bankProblem2.questionType = CourseSketch.prutil.QuestionType.FREE_RESP;
    CourseSketch.fakeProblems.push(problem2);
    CourseSketch.fakeBankProblems.push(bankProblem2);

    problem3.courseId = "1";
    problem3.assignmentId = "2";
    problem3.id = "3";
    problem3.state = CourseSketch.prutil.State();
    problem3.state.published = true;
    problem3.state.accessible = false;
    problem3.state.pastDue = false;
    problem3.state.started = false;
    problem3.state.completed = false;
    problem3.state.graded = false;
    problem3.problemInfo = bankProblem3;
    bankProblem3.id = problem3.id;
    bankProblem3.questionText = "Please select the correct answer";
    bankProblem3.questionType = CourseSketch.prutil.QuestionType.MULT_CHOICE;
    CourseSketch.fakeProblems.push(problem3);
    CourseSketch.fakeBankProblems.push(bankProblem3);

    problem4.courseId = "1";
    problem4.id = "4";
    problem1.assignmentId = "2";
    problem4.state = CourseSketch.prutil.State();
    problem4.state.published = true; //closed
    problem4.state.accessible = false;
    problem4.state.pastDue = true;
    problem4.state.started = false;
    problem4.state.completed = false;
    problem4.state.graded = false;
    problem4.problemInfo = bankProblem4;
    bankProblem4.id = problem4.id;
    bankProblem4.questionText = "Please select the correct answer (all or some!)";
    bankProblem4.questionType = CourseSketch.prutil.QuestionType.CHECK_BOX;
    CourseSketch.fakeProblems.push(problem4);
    CourseSketch.fakeBankProblems.push(bankProblem4);

    // TODO: please finish out the test data...
    problem5.courseId = "2";
    problem5.id = "5";
    problem5.assignmentId = "3";
    problem5.name = "Quantum spin states";
    problem5.state = CourseSketch.prutil.State();
    problem5.state.published = true; //completed
    problem5.state.accessible = true;
    problem5.state.pastDue = false;
    problem5.state.started = true;
    problem5.state.completed = true;
    problem5.state.graded = false;
    bankProblem5.questionText = "a cat has eaten bbq, cube steak, and kolache.  What state is it in?";
    problem5.problemInfo = bankProblem5;
    bankProblem5.id = problem5.id;
    CourseSketch.fakeProblems.push(problem5);
    CourseSketch.fakeBankProblems.push(bankProblem5);

    problem6.courseId = "2";
    problem6.id = "6";
    problem6.assignmentId = "3";
    problem6.name = "weave identification";
    problem6.state = CourseSketch.prutil.State();
    problem6.state.published = false; //not published
    problem6.state.accessible = false;
    problem6.state.pastDue = false;
    problem6.state.started = false;
    problem6.state.completed = false;
    problem6.state.graded = false;
    problem6.problemInfo = bankProblem6;
    bankProblem6.questionText = "please draw a square weave";
    bankProblem6.id = problem6.id;
    CourseSketch.fakeProblems.push(problem6);
    CourseSketch.fakeBankProblems.push(bankProblem6);

    problem7.courseId = "3";
    problem7.id = "7";
    problem7.assignmentId = "4";
    problem7.name = "underwater breathing";
    problem7.problemInfo = bankProblem7;
    bankProblem7.questionText = "please start a timer and hold your breath for 10 minutes.";
    bankProblem7.id = problem7.id;
    CourseSketch.fakeProblems.push(problem7);
    CourseSketch.fakeBankProblems.push(bankProblem7);

    problem8.courseId = "3";
    problem8.id = "8";
    problem8.assignmentId = "5";
    problem8.name = "style quiz";
    problem8.problemInfo = bankProblem8;
    bankProblem8.id = problem8.id;
    bankProblem8.questionText = "please fix the issues with the styling stuff";
    CourseSketch.fakeProblems.push(problem8);
    CourseSketch.fakeBankProblems.push(bankProblem8);

    problem9.courseId = "4";
    problem9.assignmentId = "6";
    problem9.id = "9";
    problem9.name = "make course sketch";
    problem9.problemInfo = bankProblem9;
    bankProblem9.id = problem9.id;
    bankProblem9.questionText = "not a big problem, I just want you to program a MOOC using sketch recognition.  "
                                     + "You have one week";
    CourseSketch.fakeProblems.push(problem9);
    CourseSketch.fakeBankProblems.push(bankProblem9);

    problem10.courseId = "5";
    problem10.assignmentId = "7";
    problem10.id = "10";
    problem10.name = "make course sketch";
    problem10.description = "not a big problem, I just want you to program a MOOC using sketch recognition.  "
    + "You have one week.";
    problem10.problemInfo = bankProblem10;
    bankProblem10.id = problem10.id;
    CourseSketch.fakeProblems.push(problem10);
    CourseSketch.fakeBankProblems.push(bankProblem10);

    problem11.courseId = "6";
    problem11.assignmentId = "8";
    problem11.id = "11";
    problem11.name = "make course sketch";
    problem11.description = "not a big problem, I just want you to program a MOOC using sketch recognition.  "
    + "You have one week.";
    problem11.problemInfo = bankProblem11;
    bankProblem11.id = problem11.id;
    CourseSketch.fakeProblems.push(problem11);
    CourseSketch.fakeBankProblems.push(bankProblem11);

    problem12.courseId = "6";
    problem12.assignmentId = "8";
    problem12.id = "12";
    problem12.name = "make course sketch";
    problem12.description = "not a big problem, I just want you to program a MOOC using sketch recognition.  "
    + "You have one week.";
    problem12.problemInfo = bankProblem12;
    bankProblem12.id = problem12.id;
    CourseSketch.fakeProblems.push(problem12);
    CourseSketch.fakeBankProblems.push(bankProblem12);

    problem13.courseId = "6";
    problem13.assignmentId = "9";
    problem13.id = "13";
    problem13.name = "make course sketch";
    problem13.description = "not a big problem, I just want you to program a MOOC using sketch recognition.  "
    + "You have one week.";
    problem13.problemInfo = bankProblem13;
    bankProblem13.id = problem13.id;
    CourseSketch.fakeProblems.push(problem13);
    CourseSketch.fakeBankProblems.push(bankProblem13);

    problem14.courseId = "6";
    problem14.assignmentId = "9";
    problem14.id = "14";
    problem14.name = "make course sketch";
    problem14.description = "not a big problem, I just want you to program a MOOC using sketch recognition.  "
    + "You have one week.";
    problem14.problemInfo = bankProblem14;
    bankProblem14.id = problem14.id;
    CourseSketch.fakeProblems.push(problem14);
    CourseSketch.fakeBankProblems.push(bankProblem14);
})();
