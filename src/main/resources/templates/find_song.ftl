<!DOCTYPE html>
<html lang="en">
    <#include "header.ftl">
<body>
    <#include "nav.ftl">

<div class="container">
    <div class="row center-align">
        <h3>Find a Song</h3>
    </div>

    <div class="row">
        <form class="col s12 offset-s2">
            <div class="row">
                <div class="input-field col s6">
                    <textarea id="search_bar" class="materialize-textarea" rows="1" onkeypress="main.submitSongSearchBar(event)"></textarea>
                    <label for="search_bar">Search Songs</label>
                </div>
                <div class="input-field col s4">
                    <button class="btn waves-effect waves-light" type="button" onclick="main.populateSongs()">Search
                        <i class="material-icons right">search</i>
                    </button>
                </div>
            </div>
            <div class="row">
                <a href="/">Want to create by image instead?</a>
            </div>
        </form>
    </div>

    <div id="song-container" class="row">

    </div>
</div>

    <#include "js.ftl">
</body>
</html>