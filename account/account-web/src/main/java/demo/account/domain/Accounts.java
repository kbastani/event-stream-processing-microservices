package demo.account.domain;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

import java.io.Serializable;

public class Accounts extends Resources<Account> {

    private PageModel page;

    public Accounts(Page<Account> accountPage) {
        super(accountPage.getContent());
        page = new PageModel(accountPage);
    }

    public Accounts(Iterable<Account> content, Link... links) {
        super(content, links);
    }

    public Accounts(Iterable<Account> content, Iterable<Link> links) {
        super(content, links);
    }

    public PageModel getPage() {
        return page;
    }

    class PageModel implements Serializable {

        private int number;
        private int size;
        private int totalPages;
        private long totalElements;

        public PageModel() {
        }

        public PageModel(Page page) {
            number = page.getNumber();
            size = page.getSize();
            totalPages = page.getTotalPages();
            totalElements = page.getTotalElements();
        }

        public int getNumber() {
            return number;
        }

        public int getSize() {
            return size;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public long getTotalElements() {
            return totalElements;
        }
    }
}
