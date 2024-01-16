package az.crocusoft.ecommerce.mapper;

import az.crocusoft.ecommerce.dto.BlogCategoryDto;
import az.crocusoft.ecommerce.model.BlogCategory;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-12-07T00:56:37-0800",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.4.jar, environment: Java 18.0.2 (Amazon.com Inc.)"
)
@Component
public class BlogCategoryMapperImpl implements BlogCategoryMapper {

    @Override
    public BlogCategory toEntity(BlogCategoryDto categoryDto) {
        if ( categoryDto == null ) {
            return null;
        }

        BlogCategory blogCategory = new BlogCategory();

        blogCategory.setCid( categoryDto.getCid() );
        blogCategory.setName( categoryDto.getName() );
        blogCategory.setDescription( categoryDto.getDescription() );

        return blogCategory;
    }
}
