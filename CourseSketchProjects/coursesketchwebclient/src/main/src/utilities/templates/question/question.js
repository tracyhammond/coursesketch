function Question() {
    this.lectures = [];

    /**
     * @param templateClone {node} is a clone of the custom HTML Element for the text box
     * Makes the exit button close the box and enables dragging
     */
    this.initializeElement = function(templateClone) {
        localScope = this; // This sets the variable to the level of the custom element tag
        shadowRoot = this.createShadowRoot();
        shadowRoot.appendChild(templateClone);

        shadowRoot.getElementById("actions").onclick = function(event) {
            shadowRoot.getElementById("actions-dialog").open = true;
        }
        shadowRoot.getElementById("dialog-close").onclick = function(event) {
            shadowRoot.getElementById("actions-dialog").open = false;
        }
        shadowRoot.getElementById("correct-lecture").onchange = function(event) {
            var value = event.srcElement.value;
            var lectureIndex = parseInt(event.srcElement.dataset["lecture-" + value]);
            var lecture = localScope.lectures[lectureIndex];
            localScope.loadSlides(lecture.idList, shadowRoot.getElementById("correct-slide"));
        }
        shadowRoot.getElementById("incorrect-lecture").onchange = function(event) {
            var value = event.srcElement.value;
            var lectureIndex = parseInt(event.srcElement.dataset["lecture-" + value]);
            var lecture = localScope.lectures[lectureIndex];
            localScope.loadSlides(lecture.idList, shadowRoot.getElementById("incorrect-slide"));
        }
    }

    /**
     * Loads the lectures that can be navigated to in the question.
     * @param lectureIds list of lecture IDs to load
     */
    this.loadLectures = function(lectureIds) {
        var localScope = this;
        var callback = function(lectures) {
            shadowRoot.getElementById("correct-lecture").innerHTML = "";
            shadowRoot.getElementById("incorrect-lecture").innerHTML = "";
            shadowRoot.getElementById("correct-lecture").dataset = [];
            shadowRoot.getElementById("incorrect-lecture").dataset = [];
            localScope.lectures = [];
            for (var i = 0; i < lectures.length; ++i) {
                var option = document.createElement("option");
                option.textContent = lectures[i].name;
                option.value = lectures[i].id;
                shadowRoot.getElementById("correct-lecture").dataset["lecture-" + lectures[i].id] = i;
                shadowRoot.getElementById("incorrect-lecture").dataset["lecture-" + lectures[i].id] = i;
                localScope.lectures.push(lectures[i]);
                shadowRoot.getElementById("correct-lecture").appendChild(option);
                shadowRoot.getElementById("incorrect-lecture").appendChild(option.cloneNode(true));
            }
            if (localScope.lectures.length > 0) {
                localScope.loadSlides(localScope.lectures[0].idList, shadowRoot.getElementById("correct-slide"));
                localScope.loadSlides(localScope.lectures[0].idList, shadowRoot.getElementById("incorrect-slide"));
            }
            $(localScope.shadowRoot.getElementById("actions-box")).removeClass("hide");
        }
        CourseSketch.dataManager.getCourseLectures(lectureIds, callback, callback);
    }

    /**
     * Loads slides into a slide select element.
     * @param idList list of "idsInLecture" containing the slides to load
     * @param slideSelect select element to load the slides into
     */
    this.loadSlides = function(idList, slideSelect) {
        var callback = function(slides) {
            slideSelect.innerHTML = ""
            for(var i = 0; i < slides.length; ++i) {
                var option = document.createElement("option");
                option.textContent = i + 1;
                option.value = i;
                slideSelect.appendChild(option);
            }
            if(slides.length > 0) {
                slideSelect.disabled = false;
            }
        }
        var slideIds = [];
        for(var i = 0; i < idList.length; ++i) {
            slideIds.push(idList[i].id);
        }
        CourseSketch.dataManager.getLectureSlides(slideIds, callback, callback);
    }

    /**
     * Adds multiple choice content to the question
     * @param multiChoice the MultiChoice element to add
     */
    this.addAnswerContent = function(answerContent) {
        answerContent.className = "answer";
        this.appendChild(answerContent);
    }

    this.saveData = function(event) {
        var questionProto = CourseSketch.PROTOBUF_UTIL.SrlQuestion();

        // Populate data in the proto object
        questionProto.id = generateUUID();
        questionProto.setQuestionText(this.shadowRoot.querySelector('#text').value);
        var nodes = this.shadowRoot.querySelector('content').getDistributedNodes();
        // We should really only ever have one node here
        if (nodes.length > 0) {
            if (nodes[0] instanceof MultiChoice) {
                questionProto.multipleChoice = nodes[0].saveData();
            } else if (nodes[0] instanceof SketchSurface) {
                // TODO: Need to support sketch questions
                // questionProto.ourThing = nodes[0].saveData();
                console.log("Saving sketch questions is not yet supported.");
            } else {
                throw "Attempted to save invalid answer element";
            }
        }

        // TODO: Currently just one nav for correct/incorrect; add navs for different answer choices (also needs to work with sketch)
        // TODO: Add validation; currently if the user forgets to specify this, defaults to first lecture and first slide
        var correctLecture = shadowRoot.getElementById("correct-lecture");
        var correctSlide = shadowRoot.getElementById("correct-slide");
        var incorrectLecture = shadowRoot.getElementById("incorrect-lecture");
        var incorrectSlide = shadowRoot.getElementById("incorrect-slide");
        if (!isUndefined(correctLecture) && !isUndefined(correctSlide) && !isUndefined(incorrectLecture) && !isUndefined(incorrectSlide)
                && correctLecture != null && correctSlide != null && incorrectLecture != null && incorrectSlide != null) {
            var correctLectureId = correctLecture.value;
            var correctSlideStr = correctSlide.value;
            var incorrectLectureId = incorrectLecture.value;
            var incorrectSlideStr = incorrectSlide.value;
            if (!isUndefined(correctLectureId) && !isUndefined(correctSlideStr) && !isUndefined(incorrectLectureId) && !isUndefined(incorrectSlideStr)
                    && correctLectureId !== "" && correctSlideStr !== "" && incorrectLectureId !== "" && incorrectSlideStr !== "") {
                var correctNav = CourseSketch.PROTOBUF_UTIL.LectureNavigator();
                var incorrectNav = CourseSketch.PROTOBUF_UTIL.LectureNavigator();
                correctNav.nextLectureId = correctLectureId;
                correctNav.nextSlide = parseInt(correctSlideStr);
                incorrectNav.nextLectureId = incorrectLectureId;
                incorrectNav.nextSlide = parseInt(incorrectSlideStr);
                questionProto.navs[0] = correctNav;
                questionProto.navs[1] = incorrectNav;
            }
        }

        // If the textbox does not have an id, then a command has not been created for the question
        if ((isUndefined(this.id) || this.id == null || this.id == "")) {
            this.command = CourseSketch.PROTOBUF_UTIL.createBaseCommand(CourseSketch.PROTOBUF_UTIL.CommandType.CREATE_QUESTION,true);
        }
        this.command.setCommandData(questionProto.toArrayBuffer()); // Sets commandData for commandlist
        this.createdCommand = this.command;
        this.id = this.command.commandId;
        var callback = this.getFinishedCallback();
        if(!isUndefined(callback)) {
            callback(this.command, event, this.currentUpdate); // Gets finishedCallback and calls it with command as parameter
        }
        return questionProto;
    }

    /**
     * @param textBoxProto {protoCommand} is the data to be loaded from the proto
     * If shadowRoot does not exist, saves the protoCommand locally and returns so the element can be initialized
     * If the protoCommand does not exist, returns because data cannot be loaded
     *
     * NOTE: If using navigation, this MUST be called after loadLectures has been called!
     */
    this.loadData = function(questionProto) {
        if (isUndefined(shadowRoot) || isUndefined(questionProto)) {
            return;
        }
        this.shadowRoot.querySelector("#text").value = questionProto.getQuestionText();
        var nodes = this.shadowRoot.querySelector('content').getDistributedNodes();
        if(!isUndefined(questionProto.multipleChoice) && questionProto.multipleChoice != null && nodes.length > 0 && (nodes[0] instanceof MultiChoice)) {
            var answerContent = nodes[0];
            answerContent.loadData(questionProto.multipleChoice);
        } else if(!isUndefined(questionProto.sketchQuestion) && questionProto.sketchQuestion != null) {
            throw "Sketch questions are not yet supported"
        }

        // TODO: Currently just one nav for correct/incorrect; add navs for different answer choices (also needs to work with sketch)
        if(questionProto.navs.length >= 2) {
            var correctNav = questionProto.navs[0];
            var incorrectNav = questionProto.navs[1];
            shadowRoot.getElementById("correct-lecture").value = correctNav.nextLectureId;
            shadowRoot.getElementById("correct-slide").value = correctNav.nextSlide;
            shadowRoot.getElementById("incorrect-lecture").value = incorrectNav.nextLectureId;
            shadowRoot.getElementById("incorrect-slide").value = incorrectNav.nextSlide;
        }
    }

    /**
     * @return finishedCallback {function} is the callback set at implementation.
     * The callback can be called immediately using .getFinishedCallback()(argument) with argument being optional
     */
    this.getFinishedCallback = function() {
        return this.finishedCallback;
    };

    this.setFinishedListener = function(listener) {
        this.finishedCallback = listener;
    };
}
Question.prototype = Object.create(HTMLDialogElement.prototype);
Question.prototype.finishedCallback = undefined; // Defined by whoever implements this by using setFinishedListener().
Question.prototype.createdCommand = undefined;
