(function() {
    $(document).ready(function() {
        CourseSketch.lecturePage = [];
        CourseSketch.lecturePage.waitScreenManager = new WaitScreenManager();

        CourseSketch.lecturePage.doResize = function(event) {
            var target = event.target;

            // add the change in coords to the previous width of the target element
            var newWidth  = parseFloat(target.style.width) + event.dx;
            var newHeight = parseFloat(target.style.height) + event.dy;

            // update the element's style
            target.style.width  = newWidth + 'px';
            target.style.height = newHeight + 'px';

            target.textContent = newWidth + '×' + newHeight;
        }

        CourseSketch.lecturePage.saveTextBox = function(command, event, currentUpdate) {
            var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(command.getCommandData(),
                CourseSketch.PROTOBUF_UTIL.getActionCreateTextBoxClass());
            var element = CourseSketch.PROTOBUF_UTIL.LectureElement();
            element.id = generateUUID();
            element.textBox = decoded;
            CourseSketch.lecturePage.currentSlide.elements.push(element);
        }

        CourseSketch.lecturePage.saveQuestion = function(command, event, currentUpdate) {
            var decoded = CourseSketch.PROTOBUF_UTIL.decodeProtobuf(command.getCommandData(),
                CourseSketch.PROTOBUF_UTIL.getSrlQuestionClass());
            var element = CourseSketch.PROTOBUF_UTIL.LectureElement();
            element.id = generateUUID();
            element.question = decoded;
            CourseSketch.lecturePage.currentSlide.elements.push(element);
        }

        CourseSketch.lecturePage.loadTextBox = function(textBox) {
            var elem = CourseSketch.lecturePage.newTextBox();
            elem.loadData(textBox);
        }

        CourseSketch.lecturePage.loadMultiChoiceQuestion = function(question) {
            var elem = CourseSketch.lecturePage.newMultiChoiceQuestion();
            elem.loadData(question);
        }

        /**
         * Adds a new text box to the currently selected lecture slide.
         */
        CourseSketch.lecturePage.newTextBox = function() {
            var textbox = document.createElement('text-box-creation');
            document.querySelector("#slide-content").appendChild(textbox);
            textbox.setFinishedListener(CourseSketch.lecturePage.saveTextBox);
            return textbox;
        }

        CourseSketch.lecturePage.newSketchContent = function() {
            var sketchSurface = document.createElement('sketch-surface');
            document.querySelector("#slide-content").appendChild(sketchSurface);
            setTimeout(function() {
                sketchSurface.resizeSurface();
            }, 500);
            return sketchSurface;
        }

        CourseSketch.lecturePage.newMultiChoiceQuestion = function() {
            var question = document.createElement("question-element");
            var multiChoice = document.createElement("multi-choice");
            document.getElementById("slide-content").appendChild(question);
            question.addAnswerContent(multiChoice);
            question.setFinishedListener(CourseSketch.lecturePage.saveQuestion);
            return question;
        }

        /**
         * Renders a slide to the DOM.
         *
         * NOTE: THIS FUNCTION IS NOT COMPLETE!
         *
         * @param slide
         *            protobuf slide element to be rendered
         */
        CourseSketch.lecturePage.renderSlide = function(slide) {
        document.getElementById("slide-content").innerHTML = ""
            CourseSketch.lecturePage.currentSlide = slide;
            for(var i = 0; i < slide.elements.length; ++i) {
                var element = slide.elements[i];
                if(!isUndefined(element.textBox) && element.textBox != null) {
                    CourseSketch.lecturePage.loadTextBox(element.textBox);
                } else if(!isUndefined(element.question) && element.question != null) {
                    if(!isUndefined(element.question.multipleChoice) && element.question.multipleChoice != null) {
                        CourseSketch.lecturePage.loadMultiChoiceQuestion(element.question);
                    } else {
                        throw "Sketch questions are not yet supported";
                    }
                } else {
                    throw "Tried to load invalid element";
                }
            }
        }


        /**
         * Selects a specific lecture slide.
         *
         * @param slideIndex
         *            index of the slide in the current lecture's protobuf
         *            object.
         */
        CourseSketch.lecturePage.selectSlide = function(slideIndex) {
            var completionHandler = function() {
                $(".slide-thumb").each(function() {
                    $(this).removeClass("selected");
                })
                $("#" + slideIndex + ".slide-thumb").addClass("selected");
                CourseSketch.dataManager.getLectureSlide(CourseSketch.lecturePage
                    .lecture.idList[slideIndex].id, CourseSketch.lecturePage.renderSlide,
                    CourseSketch.lecturePage.renderSlide);
                CourseSketch.lecturePage.removeWaitOverlay();
            }
            if(!isUndefined(CourseSketch.lecturePage.currentSlide)) {
                var elements = document.getElementById("slide-content").children;

                // Need to remove all the old elements; they will be replaced by the new ones
                CourseSketch.lecturePage.currentSlide.elements = [];

                for(var i = 0; i < elements.length; ++i) {
                    elements[i].saveData();
                }
                CourseSketch.lecturePage.addWaitOverlay();
                CourseSketch.dataManager.updateSlide(CourseSketch.lecturePage.currentSlide, completionHandler, completionHandler);
            } else {
                completionHandler();
            }
        }

        CourseSketch.lecturePage.addWaitOverlay = function() {
            CourseSketch.lecturePage.waitScreenManager.buildOverlay(document.querySelector("body"));
            CourseSketch.lecturePage.waitScreenManager.buildWaitIcon(document.getElementById("overlay"));
            document.getElementById("overlay").querySelector(".waitingIcon").classList.add("centered");
        }

        CourseSketch.lecturePage.removeWaitOverlay = function() {
            document.querySelector("body").removeChild(document.getElementById("overlay"));
        }

        /**
         * Adds a slide thumbnail to the DOM.
         *
         * @param slideIndex
         *            index of the slide in the current lecture's protobuf
         *            object.
         */
        CourseSketch.lecturePage.addSlideToDom = function(slideIndex) {
            var slideThumb = document.createElement("span");
            slideThumb.id = slideIndex;
            slideThumb.className = "slide-thumb";
            slideThumb.textContent = slideIndex + 1;
            slideThumb.onclick = function() {
                CourseSketch.lecturePage.selectSlide(slideIndex);
            };
            document.querySelector("#slides>.content").appendChild(slideThumb);
        }

        /**
         * Adds a new slide to the current lecture.
         */
        CourseSketch.lecturePage.newSlide = function() {
            var slide = CourseSketch.PROTOBUF_UTIL.LectureSlide();
            slide.id = generateUUID();
            slide.lectureId = CourseSketch.lecturePage.lecture.id;
            slide.unlocked = true;
            var finishGetCourse = function(lecture) {
                var id = CourseSketch.PROTOBUF_UTIL.IdsInLecture();
                id.id = slide.id;
                id.isSlide = true;
                id.unlocked = true;
                CourseSketch.lecturePage.lecture.idList.push(id);
                CourseSketch.lecturePage.displaySlides();
            }
            var finishInsert = function(lecture) {
                CourseSketch.dataManager.getCourseLecture(CourseSketch.lecturePage
                    .lecture.id, finishGetCourse, finishGetCourse);
            }
            CourseSketch.dataManager.insertSlide(slide, finishInsert, finishInsert);
        }

        /**
         * Displays all of the slides for the current lecture.
         */
        CourseSketch.lecturePage.displaySlides = function() {
            $("#lecture-title").text(CourseSketch.lecturePage.lecture.name);
            $(".slide-thumb:not(\"#add\")").each(function() {
                $(this).remove();
            });
            for(var i = 0; i < CourseSketch.lecturePage.lecture.idList.length; ++i) {
                CourseSketch.lecturePage.addSlideToDom(i);
            }
            if(CourseSketch.lecturePage.lecture.idList.length > 0) {
                CourseSketch.lecturePage.selectSlide(0);
            }
        }

        CourseSketch.lecturePage.addPic = function(input) {
            var imagebox = document.createElement('img');
            imagebox.className='resize';
            if (input.files && input.files[0]) {
                var reader = new FileReader();
                reader.onload = function (e) {
                    $(imagebox)
                        .attr('src', e.target.result)
                        .width(100)
                        .height(100);
                };

                reader.readAsDataURL(input.files[0]);
            }
            document.querySelector("#slide-content").appendChild(imagebox);
        }

        // Do setup
        if (CourseSketch.dataManager.isDatabaseReady() && isUndefined(CourseSketch.lecturePage.lecture)) {
            CourseSketch.lecturePage.lecture = CourseSketch.dataManager.getState("currentLecture");
            CourseSketch.dataManager.clearStates();
            CourseSketch.lecturePage.displaySlides();
        } else {
            var intervalVar = setInterval(function() {
            if (CourseSketch.dataManager.isDatabaseReady() && isUndefined(CourseSketch.lecturePage.lecture)) {
                clearInterval(intervalVar);
                    CourseSketch.lecturePage.lecture = CourseSketch.dataManager.getState("currentLecture");
            CourseSketch.dataManager.clearStates();
                    CourseSketch.lecturePage.displaySlides();
            }
            }, 100);
        }
        interact('.resize').resizable(true).on('resizemove', CourseSketch.lecturePage.doResize);
    });
})();
