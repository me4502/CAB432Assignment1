"use strict";

function populateImages() {
  var children = document.querySelector("#image-container");

  while (children.hasChildNodes()) {
    children.removeChild(children.lastChild);
  }

  var search = document.querySelector("#search_bar").value;
}