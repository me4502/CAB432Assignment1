<!DOCTYPE html>
<html lang="en">
    <#include "header.ftl">
<body>
    <#include "nav.ftl">

    <div class="container">
        <div class="row">
            <h3>Refine Your Tags</h3>
        </div>

        <div id="tag-container" class="row">
            <p>Loading Tags... Please wait!</p>
        </div>

        <div class="row">
            <button class="btn waves-effect waves-light" type="button" onclick="main.moveToExportPage()">Confirm Tags
                <i class="material-icons right">submit</i>
            </button>
        </div>
    </div>

    <#include "js.ftl">
    <script>
        main.loadTags();
    </script>
</body>
</html>