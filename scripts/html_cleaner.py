from bs4 import BeautifulSoup


def replace_collapsible_tags_with_h2(html_file):
    with open(html_file, 'r', encoding='utf-8') as file:
        soup = BeautifulSoup(file, 'html.parser')

    collapsible_divs = soup.find_all(class_='wrap-collapsible')

    for div in collapsible_divs:
        build_label = div.find(class_='lbl-toggle')
        content = div.find(class_='content-inner')

        if build_label and content:
            build_number = build_label.get_text()
            h2_tag = soup.new_tag('h2')
            h2_tag.string = build_number
            div.replace_with(h2_tag, content)

    with open(html_file, 'w', encoding='utf-8') as file:
        file.write(str(soup))


if __name__ == '__main__':
    input_html_file = 'input.html'  # Replace with your input HTML file
    replace_collapsible_tags_with_h2(input_html_file)
    print(f'Replaced collapsible tags with h2 tags in {input_html_file}')
