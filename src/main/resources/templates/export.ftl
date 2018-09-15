<!DOCTYPE html>
<html lang="en">
    <#include "header.ftl">
<body>
    <#include "nav.ftl">

<div class="container">
    <div class="row">
        <h3>Finalise The Image</h3>
    </div>

    <div class="row">
        <p id="song-name">Song Name: Loading...</p>
    </div>

    <canvas id="export-canvas">

    </canvas>
</div>

    <#include "js.ftl">
    <script>
        main.loadExport();
    </script>
</body>
</html>