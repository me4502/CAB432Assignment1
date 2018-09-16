<!DOCTYPE html>
<html lang="en">
    <#include "header.ftl">
<body>
    <#include "nav.ftl">

    <div class="container">
        <div class="row center-align">
            <h3>Refine Your Tags</h3>
        </div>

        <div id="tag-container" class="row">
            <p>Loading Tags... Please wait!</p>
        </div>

        <br/>
        <br/>

        <div class="row center-align">
            <button id="confirm-button" class="btn waves-effect waves-light" style="display: none;" type="button" onclick="main.moveToExportPage()">Confirm Tags
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