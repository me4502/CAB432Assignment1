import {API_URL, checkForStorage} from "./util";

export function loadTags() {
    if (checkForStorage('chosenImage') && checkForStorage('chosenImageURL')) {
        const children = document.querySelector("#tag-container");
        const confirmButton = document.querySelector("#confirm-button");

        let image = localStorage.getItem('chosenImage');
        Promise.all([
            window.fetch(API_URL + '/tag/popular').then(r => r.json()),
            window.fetch(API_URL + '/image/tag/' + image).then(r => r.json())
        ]).then(jsonTuple => {
            let popularTags = jsonTuple[0];
            let foundTags = jsonTuple[1];
            let tags = [];
            for (let tag in foundTags) {
                tag = foundTags[tag];
                tags.push({'tag': tag});
            }
            let autoTags = {};
            for (let autoTag in popularTags) {
                autoTag = popularTags[autoTag];
                autoTags[autoTag] = null;
            }

            while (children.hasChildNodes()) {
                children.removeChild(children.lastChild);
            }
            children.innerHTML = "<div class=\"chips\"></div>";
            const chips = document.querySelector(".chips");
            M.Chips.init(chips, {
                data: tags,
                autocompleteOptions: {
                    data: autoTags,
                }
            });

            confirmButton.style.display = "";
        }).catch(ex => {
            while (children.hasChildNodes()) {
                children.removeChild(children.lastChild);
            }
            children.innerHTML = "<p>Failed to load tags: " + ex.message +"</p>";
            console.log('Failed to parse', ex)
        });
    }
}

export function moveToExportPage() {
    const chips = document.querySelector(".chips");
    const tags = [];

    let inst = M.Chips.getInstance(chips);
    for (let i in inst.chipsData) {
        tags.push(inst.chipsData[i]['tag']);
    }

    if (tags.length === 0) {
        M.toast({html: 'You must select at least one tag!'})
    } else {
        localStorage.setItem("chosenTags", tags);
        localStorage.setItem("exportMode", "image");
        window.location.href = "/export";
    }
}
