<!DOCTYPE html>
<html lang="en">
    <#include "header.ftl">
<body>
    <#include "nav.ftl">

    <div class="container">
        <div class="row">
            <h3>Find an Image</h3>
        </div>

        <div class="row">
            <form class="col s12 offset-s2">
                <div class="row">
                    <div class="input-field col s6">
                        <textarea id="search_bar" class="materialize-textarea" rows="1" onkeypress="main.submitSearchBar(event)"></textarea>
                        <label for="search_bar">Search Images</label>
                    </div>
                    <div class="input-field col s4">
                        <button class="btn waves-effect waves-light" type="button" onclick="main.populateImages()">Search
                            <i class="material-icons right">search</i>
                        </button>
                    </div>
                </div>
            </form>
        </div>

        <div id="image-container" class="row">

        </div>
    </div>

    <#include "js.ftl">
</body>
</html>