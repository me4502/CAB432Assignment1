<!DOCTYPE html>
<html lang="en">
    <#include "header.ftl">
<body>
    <#include "nav.ftl">

    <div class="container">
        <div class="row center-align">
            <h3>Export The Image</h3>
        </div>

        <div class="row">
            <p id="song-name">Song Name: Loading... This may take a minute...</p>
        </div>

        <div class="row center-align" id="canvas-container">
            <canvas id="export-canvas">
                Your browser does not support Canvas, sorry.
            </canvas>
        </div>

        <br/>
        <br/>

        <div class="row center-align">
            <a id="download" style="display: none;" download="export.png">
                <button type="button" class="btn waves-effect waves-light" onClick="main.downloadCanvas()">Download</button>
            </a>
        </div>
    </div>

    <#include "js.ftl">
    <script>
        main.loadExport();
    </script>
</body>
</html>