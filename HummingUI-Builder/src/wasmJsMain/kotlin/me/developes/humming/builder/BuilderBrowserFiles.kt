@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package me.developes.humming.builder

import me.developes.humming.builder.ui.*

import kotlin.js.JsString
import kotlin.js.toJsString

/** Browser picker/download; no filesystem path or network operation comes from a contract. */
class BuilderBrowserFiles : StudioFiles {
    override fun open(result: (StudioFileResult) -> Unit) {
        chooseFile { content, error -> result(StudioFileResult(content?.toString(), error?.toString())) }
    }

    override fun download(name: String, content: String) { downloadFile(name.toJsString(), content.toJsString()) }
}

@JsFun("""(done) => {
    const input = document.createElement('input');
    input.type = 'file'; input.accept = '.json,application/json'; input.style.display = 'none';
    let finished = false;
    const finish = (text, error) => { if (!finished) { finished = true; input.remove(); done(text, error); } };
    input.addEventListener('cancel', () => finish(null, null), {once:true});
    input.addEventListener('change', async () => {
        try {
            const file = input.files[0];
            if (!file) return finish(null, null);
            if (file.size > 32000000) return finish(null, 'FILE_LIMIT: arquivo excede 32 MB');
            const text = await file.text();
            if (text.length > 8000000) return finish(null, 'INPUT_LIMIT: arquivo excede 2 milhÃµes de caracteres');
            finish(text, null);
        } catch (_) { finish(null, 'FILE_READ: nÃ£o foi possÃ­vel ler o arquivo'); }
    }, {once:true});
    document.body.append(input); input.click();
}""")
private external fun chooseFile(done: (JsString?, JsString?) -> Unit)

@JsFun("""(name, text) => {
    const url = URL.createObjectURL(new Blob([text], {type:'application/json;charset=utf-8'}));
    const link = document.createElement('a'); link.href = url; link.download = name;
    document.body.append(link); link.click(); link.remove();
    setTimeout(() => URL.revokeObjectURL(url), 60000);
}""")
private external fun downloadFile(name: JsString, text: JsString)
