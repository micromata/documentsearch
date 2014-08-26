$(function() {
    // select whole text on input focus
    $(document).on("click", "input:text", function () {
        $(this).select();
    });
    // focus input field on form-control-feedback click
    $(document).on("click", ".form-control-feedback", function () {
        $(this).siblings("input:text").click();
    });
    // focus on domReady
    $("#searchField").click();
});