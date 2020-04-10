import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

public class KeywordProcessor {

    public String entity_name;
    private String _keyword = null;
    private Set<String> _white_space_chars = new HashSet<String>();
    private Integer _terms_in_trie;

    public Map<String, Object> keyword_trie_dict;
    public boolean case_sensitive;
    public List<String> word_boundaries = Arrays.asList(new String[]{" ", "\t", "\n", ",", "."});

    public KeywordProcessor (String entity_name) {
        this.entity_name = entity_name;
        this._keyword = "_keyword_";
        this._white_space_chars = new HashSet<>(Arrays.asList(".", "\t", "\n", " ", ","));
        this.keyword_trie_dict = new HashMap<>();
        this.case_sensitive = false;
        this._terms_in_trie = 0;
    }

    private Integer __len__(){
        return this._terms_in_trie;
    }

    //
    private boolean __contains__(String word){
        /*
            To check if word is present in the keyword_trie_dict
            Args:
                word : string word that you want to check

            Returns:
               status : bool
                If word is present as it is in keyword_trie_dict then we return True, else False

            Examples:
                    >>> keyword_processor.add_keyword('Big Apple')
                    >>> 'Big Apple' in keyword_processor
                    >>> # True
        */
        if(!this.case_sensitive)
            word = word.toLowerCase();
        Map<String, Object> current_dict = this.keyword_trie_dict;
        Integer len_covered = 0;
        char[] chars = word.toCharArray();
        for(char ch: chars){
            if(current_dict.containsKey( "" + ch)){
                current_dict = (Map<String, Object>) current_dict.get(ch);
                len_covered += 1;
            }
            else
                break;
        }

        return current_dict.containsKey(this._keyword) && len_covered == word.length();

    }


    private Map<String, Object> __getitem__(String word) {
        /*
        if word is present in keyword_trie_dict return the clean name for it.
            Args:
            word : string
            word that you want to check

            Returns:
            keyword : string
            If word is present as it is in keyword_trie_dict then we return keyword mapped to it.

            Examples:
                    >>> keyword_processor.add_keyword('Big Apple', 'New York')
                    >>> keyword_processor['Big Apple']
                    >>> # New York
         */
        if(!this.case_sensitive)
            word = word.toLowerCase();

        Map<String, Object> current_dict = this.keyword_trie_dict;
        Integer len_covered = 0;
        char[] chars = word.toCharArray();
        for(char ch: chars){
            if(current_dict.containsKey( "" + ch)){
                current_dict = (Map<String, Object>) current_dict.get(ch);
                len_covered += 1;
            }
            else
                break;
        }
        if (current_dict.containsKey(this._keyword) && len_covered == word.length())
            return (Map<String, Object>) current_dict.get(this._keyword);

        return null;
    }


    private boolean __setitem__(String keyword, String clean_name){
        /*
        To add keyword to the dictionary
        pass the keyword and the clean name it maps to.

        Args:
        keyword : string
        keyword that you want to identify

        clean_name : string
        clean term for that keyword that you would want to get back in return or replace
                    if not provided, keyword will be used as the clean name also.

        Examples:
            >>> keyword_processor['Big Apple'] = 'New York'
        */
        boolean status = false;
        if((clean_name == null || clean_name.isEmpty()) && !keyword.isEmpty())
            clean_name = keyword;

        if(!keyword.isEmpty() && !clean_name.isEmpty())
            if (!this.case_sensitive)
                keyword = keyword.toLowerCase();

        Map<String, Object> current_dict = this.keyword_trie_dict;
        char[] chars = keyword.toCharArray();

        for(char ch: chars){
            if (!current_dict.containsKey("" + ch))
                current_dict.put("" + ch, new HashMap<>());
            current_dict = (Map<String, Object>) current_dict.get("" + ch);
        }
        if (!current_dict.containsKey(this._keyword)) {
            status = true;
            this._terms_in_trie += 1;
        }
        current_dict.put(this._keyword, clean_name);

        return status;
    }


    private boolean __delitem__(String keyword){
        /*
        To remove keyword from the dictionary
        pass the keyword and the clean name it maps to.

        Args:
        keyword : string
        keyword that you want to remove if it's present

        Examples:
                >>> keyword_processor.add_keyword('Big Apple')
                >>> del keyword_processor['Big Apple']
        */
        boolean status = false;
        if (!keyword.isEmpty()){
            if(!this.case_sensitive)
                keyword = keyword.toLowerCase();

            Map<String, Object> current_dict = this.keyword_trie_dict;
            List<List<Object>> character_trie_list = new ArrayList<>();
            char[] chars = keyword.toCharArray();

            for(char ch: chars){
                if(current_dict.containsKey( "" + ch)){
                    // revisit
                    List<Object> chmap = new ArrayList<>();
                    chmap.add(ch);
                    chmap.add(current_dict);
                    character_trie_list.add(chmap);
                    current_dict = (Map<String, Object>) current_dict.get(ch);
                }
                else {
                    // if character is not found, break out of the loop
                    current_dict = null;
                    break;
                }
            }
            // remove the characters from trie dict if there are no other keywords with them
            if (current_dict!=null && current_dict.containsKey(this._keyword)){
                // we found a complete match for input keyword.
                List<Object> keywordmap = new ArrayList<>();
                keywordmap.add(this._keyword);
                keywordmap.add(current_dict);

                character_trie_list.add(keywordmap);
                Collections.reverse(character_trie_list);


                for (List item : character_trie_list) {
                    String key_to_remove = (String) item.get(0);
                    Map<String, Object> dict_pointer = (Map<String, Object>) item.get(1);

                    if (dict_pointer.size() == 1)
                        dict_pointer.remove(key_to_remove);

                    else {
                        // more than one key means more than 1 path.
                        // Delete not required path and keep the other
                        dict_pointer.remove(key_to_remove);
                        break;
                    }
                }
                // # successfully removed keyword
                status = true;
                this._terms_in_trie -= 1;
            }

        }
        return status;
    }


    public boolean add_keyword(String keyword, String clean_name){
        /*
            To add one or more keywords to the dictionary
            pass the keyword and the clean name it maps to.

            Args:
            keyword : string
            keyword that you want to identify

            clean_name : string
            clean term for that keyword that you would want to get back in return or replace
                        if not provided, keyword will be used as the clean name also.

            Returns:
            status : bool
            The return value. True for success, False otherwise.

            Examples:
                    >>> keyword_processor.add_keyword('Big Apple', 'New York')
                    >>> # This case 'Big Apple' will return 'New York'
                    >>> # OR
                    >>> keyword_processor.add_keyword('Big Apple')
                            >>> # This case 'Big Apple' will return 'Big Apple'
        */
        return this.__setitem__(keyword, clean_name);
    }

    public boolean remove_keyword(String keyword) {
        /*
        To remove one or more keywords from the dictionary

        pass the keyword and the clean name it maps to.

        Args:
        keyword : string
        keyword that you want to remove if it's present

        Returns:
        status : bool
        The return value. True for success, False otherwise.

        Examples:
                >>> keyword_processor.add_keyword('Big Apple')
                >>> keyword_processor.remove_keyword('Big Apple')
                >>> # Returns True
                >>> # This case 'Big Apple' will no longer be a recognized keyword
                >>> keyword_processor.remove_keyword('Big Apple')
                        >>> # Returns False

         */
        return this.__delitem__(keyword);
    }

    public Map <String, Object> get_keyword(String word) {
        /*
            if word is present in keyword_trie_dict return the clean name for it.

            Args:
            word:
            string
            word that you want to check

            Returns:
            keyword:
            string
            If word is present as it is in keyword_trie_dict then we return keyword mapped to it.

                    Examples:
                >>>keyword_processor.add_keyword('Big Apple', 'New York')
                    >>> keyword_processor.get('Big Apple')
                    >>> #New York
        */
        return this.__getitem__(word);
    }

    public void add_keywords_from_dict(Map<String, ArrayList<String>>keyword_dict) {
        /*
            To add keywords from a dictionary
            Args:
            keyword_dict(dict):A dictionary with `str` key and (list `str`)as value

            Examples:
                >>>keyword_dict = {
                    "java": ["java_2e", "java programing"],
                    "product management": ["PM", "product manager"]
            }
            >>>keyword_processor.add_keywords_from_dict(keyword_dict)

            Raises:
                AttributeError:
                If value for a key in `keyword_dict` is not a list.
        */
        for (Map.Entry<String,ArrayList<String>> entry : keyword_dict.entrySet()){
            String clean_name = entry.getKey();
            ArrayList<String> keywords = entry.getValue();

            for (String keyword: keywords)
                this.add_keyword(keyword, clean_name);
        }
    }

    public void remove_keywords_from_dict(Map<String, ArrayList<String>>keyword_dict){
        /*
            To remove keywords from a dictionary
            Args:
            keyword_dict (dict): A dictionary with `str` key and (list `str`) as value

            Examples:
                    >>> keyword_dict = {
                "java": ["java_2e", "java programing"],
                "product management": ["PM", "product manager"]
            }
                    >>> keyword_processor.remove_keywords_from_dict(keyword_dict)

            Raises:
            AttributeError: If value for a key in `keyword_dict` is not a list.

         */
        for (Map.Entry<String,ArrayList<String>> entry : keyword_dict.entrySet()){
            String clean_name = entry.getKey();
            ArrayList<String> keywords = entry.getValue();

            for (String keyword: keywords)
                this.remove_keyword(keyword);
        }

    }

    public void add_keywords_from_list(ArrayList<String> keyword_list){
        /*
        To add keywords from a list

        Args:
        keyword_list (list(str)): List of keywords to add

        Examples:
                >>> keyword_processor.add_keywords_from_list(["java", "python"]})
            Raises:
            AttributeError: If `keyword_list` is not a list.
         */
        for (String keyword: keyword_list)
            this.add_keyword(keyword, null);
    }

    public void remove_keywords_from_list(ArrayList<String> keyword_list){
        /*
        To remove keywords present in list

            Args:
            keyword_list (list(str)): List of keywords to remove

            Examples:
                    >>> keyword_processor.remove_keywords_from_list(["java", "python"]})
                Raises:
                AttributeError: If `keyword_list` is not a list.

         */
        for (String keyword: keyword_list)
            this.remove_keyword(keyword);
    }

    public Map<String, String> get_all_keywords(String term_so_far,
                                                Map<String, Object> current_dict){
        /*
        Recursively builds a dictionary of keywords present in the dictionary
        And the clean name mapped to those keywords.

        Args:
        term_so_far : string
        term built so far by adding all previous characters
        current_dict : dict
        current recursive position in dictionary

        Returns:
        terms_present : dict
        A map of key and value where each key is a term in the keyword_trie_dict.
        And value mapped to it is the clean name mapped to it.

        Examples:
                >>> keyword_processor = KeywordProcessor()
                >>> keyword_processor.add_keyword('j2ee', 'Java')
                        >>> keyword_processor.add_keyword('Python', 'Python')
                        >>> keyword_processor.get_all_keywords()
                        >>> {'j2ee': 'Java', 'python': 'Python'}
                >>> # NOTE: for case_insensitive all keys will be lowercased.

         */
        Map <String, String> terms_present = new HashMap<>();

        if (term_so_far.isEmpty())
            term_so_far = "";

        if(current_dict == null)
            current_dict = this.keyword_trie_dict;

        for (Map.Entry<String, Object> entry : current_dict.entrySet()) {
            String key = entry.getKey();
            if (key.equals(this._keyword)){
                terms_present.put(term_so_far, (String) current_dict.get(key));
            }
            else{
                Map <String, String> sub_values = this.get_all_keywords(
                        term_so_far + key, (Map<String, Object>) current_dict.get(key));

                for (Map.Entry<String, String> subentry : sub_values.entrySet()) {
                    String subkey = subentry.getKey();
                    terms_present.put(subkey, sub_values.get(subkey));
                }

            }
        }

        return terms_present;

    }

    public ArrayList<ArrayList<Object>> extract_keywords(String sentence){
        /*
        Searches in the string for all keywords present in corpus.
        Keywords present are added to a list `keywords_extracted` and returned.

        Args:
        sentence (str): Line of text where we will search for keywords

        Returns:
        keywords_extracted (list(str)): List of terms/keywords found in sentence that match our corpus

        Examples:
                >>> from flashtext import KeywordProcessor
                >>> keyword_processor = KeywordProcessor()
                >>> keyword_processor.add_keyword('Big Apple', 'New York')
                        >>> keyword_processor.add_keyword('Bay Area')
                        >>> keywords_found = keyword_processor.extract_keywords('I love Big Apple and Bay Area.')
                >>> keywords_found
                >>> ['New York', 'Bay Area']
         */

        ArrayList<ArrayList<Object>> keywords_extracted = new ArrayList<>();

        if(sentence.isEmpty())
            return keywords_extracted;

        if(!this.case_sensitive)
            sentence = sentence.toLowerCase();

        Map<String, Object> current_dict = this.keyword_trie_dict;
        Integer sequence_start_pos = 0;
        Integer sequence_end_pos = 0;
        boolean reset_current_dict = false;
        Integer idx = 0;
        Integer sentence_len = sentence.length();
        while(idx < sentence_len) {
            char ch = sentence.charAt(idx);

            if (this.word_boundaries.contains("" + ch)){
                // if end is present in current_dict
                if (current_dict.containsKey(this._keyword) || current_dict.containsKey( "" + ch)) {
                    // update longest sequence found
                    String sequence_found = null;
                    String longest_sequence_found = null;
                    boolean is_longer_seq_found = false;

                    if (current_dict.containsKey(this._keyword)) {
                        sequence_found = (String) current_dict.get(this._keyword);
                        longest_sequence_found = (String) current_dict.get(this._keyword);
                        sequence_end_pos = idx;
                    }

                    // re look for longest_sequence from this position
                    if (current_dict.containsKey( "" + ch)) {
                        Map<String, Object> current_dict_continued = (Map<String, Object>) current_dict.get("" + ch);
                        Integer idy = idx + 1;
                        while (idy < sentence_len) {
                            char inner_char = sentence.charAt(idy);
                            if (this.word_boundaries.contains("" + inner_char) &&
                                    current_dict_continued.containsKey(this._keyword)) {
                                // update longest sequence found
                                longest_sequence_found = (String) current_dict_continued.get(this._keyword);
                                sequence_end_pos = idy;
                                is_longer_seq_found = true;
                            }
                            if (current_dict_continued.containsKey("" + inner_char))
                                current_dict_continued = (Map<String, Object>) current_dict_continued.get("" + inner_char);
                            else
                                break;
                            idy += 1;
                        }

                        if (current_dict_continued.containsKey(this._keyword)) {
                            // update longest sequence found
                            String current_white_space = "";
                            longest_sequence_found = (String) current_dict_continued.get(this._keyword);
                            sequence_end_pos = idy;
                            is_longer_seq_found = true;
                        }

                        if (is_longer_seq_found)
                            idx = sequence_end_pos;
                    }
                    current_dict = this.keyword_trie_dict;
                    if(longest_sequence_found != null && !longest_sequence_found.isEmpty()){
                        ArrayList<Object> keyword_info = new ArrayList<>();
                        keyword_info.add(longest_sequence_found);
                        keyword_info.add(sequence_start_pos);
                        keyword_info.add(idx);
                        keywords_extracted.add(keyword_info);
                        reset_current_dict = true;
                    }

                }
                else {
                    current_dict = this.keyword_trie_dict;
                    reset_current_dict = true;
                }
            }
            else if(current_dict.containsKey("" + ch)){
                 //we can continue from this char
                current_dict = (Map<String, Object>) current_dict.get("" + ch);
            }
            else {
                // we reset current_dict
                current_dict = this.keyword_trie_dict;
                reset_current_dict = true;

                // skip to end of word
                Integer idy = idx + 1;
                while(idy < sentence_len){
                    char chy = sentence.charAt(idy);
                    if (this.word_boundaries.contains("" + chy))
                        break;
                    idy += 1;
                }
                idx = idy;
            }
            // if we are end of sentence and have a sequence discovered
            if(idx + 1 >= sentence_len){
                if(current_dict.containsKey(this._keyword)) {
                    String sequence_found = (String) current_dict.get(this._keyword);
                    ArrayList<Object> keyword_info = new ArrayList<>();
                    keyword_info.add(sequence_found);
                    keyword_info.add(sequence_start_pos);
                    keyword_info.add(sentence_len);
                    keywords_extracted.add(keyword_info);
                }
            }
            idx += 1;
            if(reset_current_dict){
                reset_current_dict = false;
                sequence_start_pos = idx;
            }
        }
        return keywords_extracted;
    }

}
