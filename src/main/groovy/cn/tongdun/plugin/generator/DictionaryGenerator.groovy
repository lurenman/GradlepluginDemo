package cn.tongdun.plugin.generator
/**
 * 生成混淆字典字符串，注意char和 Random的使用和python不一样
 */
class DictionaryGenerator {
    static def TAG = "DictionaryGenerator >"
    private static def dict_array = [
            ["K", "k", "₭"],
            ["P", "₱", "₣"],
            ["E", "£", "₤"],
            ["₵", "¢", "₲"],
    ]
    /**
     * 生成混淆字典
     */
    static def generatorDicFile = { File file ->
        def dictText = [] as HashSet
        def mix_len = 5, max_len = 20
        def random = new Random()
        def dicts = dict_array[random.nextInt(dict_array.size())]
        println("${TAG} Choice dict:" + dicts.toString())
        for (i in 0..10000) {
            // def scope = mix_len..max_len
            def rd = new Random()
            def length = rd.nextInt(max_len - mix_len + 1) + mix_len
            def value = String.valueOf(first_char(dicts)) + generation(dicts, length)
            dictText.add(value)
        }
        file.withWriter {
            writer ->
                dictText.each {
                    line ->
                        writer.append(line + "\r\n")
                }
        }
    }

    static def first_char(def dicts) {
        while (true) {
            def random = new Random()
            def charS = dicts[random.nextInt(dicts.size())]
            def charC = charS.charAt(0)
            def charAscii = charC as Integer
            if (charAscii > 1000) {
                return charC
            }
            if (65 <= charAscii && charAscii <= 90)
                return charC
            if (97 <= charAscii && charAscii <= 122)
                return charC
            if (charAscii == 95 || charAscii == 36)
                return charC
        }
    }

    static def generation(def dicts, def length) {
        if (length > 0) {
            def random = new Random()
            def charC = dicts[random.nextInt(dicts.size())]
            length = length - 1
            charC += generation(dicts, length)
            return charC
        } else {
            return dicts[new Random().nextInt(dicts.size())]
        }
    }
}