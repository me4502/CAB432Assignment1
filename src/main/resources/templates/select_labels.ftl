<!DOCTYPE html>
<html lang="en">
    <#include "header.ftl">
<body>
    <#include "nav.ftl">

    <div class="container">
        <div class="row center-align">
            <h3>Refine Your Labels</h3>
        </div>

        <div id="label-container" class="row">
            <p>Loading Labels... Please wait!</p>
        </div>

        <br/>
        <br/>

        <div class="row center-align">
            <button id="confirm-button" class="btn waves-effect waves-light" style="display: none;" type="button" onclick="main.moveToSongExportPage()">Confirm Labels
                <i class="material-icons right">submit</i>
            </button>
        </div>
    </div>

        <#include "js.ftl">
    <script>
        main.loadLabels();
    </script>
</body>
</html>