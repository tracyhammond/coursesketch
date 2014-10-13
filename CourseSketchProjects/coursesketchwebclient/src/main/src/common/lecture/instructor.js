 
var foo = [
           {
           "title":"this is a title",
           "summary":"this is a summary"
           }
           ];
$(document).ready(function() {
                  $.each($(".list-item"), function() {
                         var children = $(this).children();
                         if(typeof children[0] != "undefined")
                         $(this).bind("click", function() {
                                      $("#placeholder").text($(children[0]).text());
                                      $.each($(".list-item"), function() {
                                             $(this).removeClass("selected");
                                             });
                                      $(this).addClass("selected");
                                      });
                         });
                  })
 