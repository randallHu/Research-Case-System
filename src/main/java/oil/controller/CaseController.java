package oil.controller;

import oil.listener.InitListener;
import oil.model.Case;
import oil.model.Doc;
import oil.model.Tag;
import oil.model.Type;
import oil.service.CaseService;
import oil.service.DocService;
import oil.service.TagService;
import oil.service.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.security.RolesAllowed;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by  waiter on 18-9-22  下午6:01.
 *
 * @author waiter
 */
@Controller
@RequestMapping(value = "/case")
public class CaseController {
  @Autowired
  private CaseService caseService;
  @Autowired
  private TagService tagService;
  @Autowired
  private TypeService typeService;
  @Autowired
  private DocService docService;
  @Value("${doc.path}")
  private String basePath;

  /**
   * 通过id获取
   *
   * @param c
   * @param model
   * @return
   */
  @GetMapping(value = "/{id}/case_info.html")
  public String getCaseById(@PathVariable(name = "id") Case c, Model model) {
    c.setTimes(c.getTimes() + 1);
    caseService.changTimes(c);
    List<Case> top10ByTypeAndIsExistOrderByTimes = caseService.findTop10ByTypeAndIsExistOrderByTimes(c.getType());
    model.addAttribute("case", c);
    model.addAttribute("top", top10ByTypeAndIsExistOrderByTimes);
    return "front/case";
  }

  /**
   * 通过日期查找，精确到月 2018-08
   *
   * @param date
   * @param page
   * @param model
   * @return
   * @throws ParseException
   */
  @GetMapping(value = {"/{date}/{page}/date.html", "/{date}/date.html"})
  public String getCasesByDate(@PathVariable(name = "date") String date,
                               @PathVariable(name = "page", required = false) Integer page,
                               Model model) throws ParseException {
    if (page == null) {
      page = 0;
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
    Page<Case> casesByDate = caseService.getCasesByDate(simpleDateFormat.parse(date), page);
    model.addAttribute("cases", casesByDate);
    model.addAttribute("type", date);

    return "front/date";
  }


  /**
   * 根据一个标签查找
   *
   * @param tag
   * @param page
   * @param model
   * @return
   */
  @GetMapping(value = {"/{tag}/{page}/tag.html", "/{tag}/tag.html"})
  public String getCasesByTag(@PathVariable(name = "tag") Tag tag,
                              @PathVariable(name = "page", required = false) Integer page,
                              Model model) {
    if (page == null) {
      page = 0;
    }

    Page<Case> allByTagsContaining = caseService.findAllByTagsContaining(page, tag);
    model.addAttribute("cases", allByTagsContaining);
    model.addAttribute("type", tag);

    return "front/tag";
  }

  /**
   * 根据类别
   *
   * @param type
   * @param page
   * @param model
   * @return
   */
  @GetMapping(value = {"/{type}/{page}/type.html", "/{type}/type.html"})
  public String getCasesByType(@PathVariable(name = "type") Type type,
                               @PathVariable(name = "page", required = false) Integer page,
                               Model model) {
    if (page == null) {
      page = 0;
    }

    Page<Case> allByType = caseService.findAllByType(page, type);
    model.addAttribute("cases", allByType);
    model.addAttribute("type", type);

    return "front/more";
  }

  /**
   * 回收列表
   *
   * @param model
   * @return
   */
  @GetMapping(value = {"/recovery"})
  public String getRecovery(Model model) {
    model.addAttribute("cases", caseService.findAllByIsExist(false));
    return "admin/recycleBin";
  }

  /**
   * 回收
   *
   * @param c
   * @return
   */
  @GetMapping(value = "/recovery/{id}")
  public String recovery(@PathVariable(name = "id") Case c, Model model) {
    c.setIsExist(true);
    List<Tag> tags = c.getTags();
    for (Tag tag : tags) {
      List<Case> cases = tag.getCases();
      cases.add(c);
      tag.setCases(cases);
    }
    tagService.saveAll(tags);
    caseService.save(c);
    return getAll(model);
  }

  /**
   * 彻底删除案例
   *
   * @param c
   * @return
   */
  @Transactional(rollbackFor = Exception.class)
  @GetMapping(value = "/delete/{id}")
  public String deleteCase(@PathVariable(name = "id") Case c, Model model) {
    if (c != null) {
      caseService.delete(c);
    }
    return getAll(model);
  }

  /**
   * 删除案例
   *
   * @param c
   * @return
   */
  @Transactional(rollbackFor = Exception.class)
  @GetMapping(value = "/remove/{id}")
  public String removeCase(@PathVariable(name = "id", required = false) Case c, Model model) {
    if (c != null) {
      List<Tag> tags = c.getTags();
      for (Tag tag : tags) {
        List<Case> cases = tag.getCases();
        cases.remove(c);
        tag.setCases(cases);
      }
      tagService.saveAll(tags);
      c.setIsExist(false);
      caseService.save(c);
    }
    return getRecovery(model);
  }

  /**
   * 添加案例
   *
   * @param c
   * @return
   */
  @Transactional(rollbackFor = Exception.class)
  @PostMapping(value = "/add.html")
  public String addCase(Case c,
                        Model model,
                        String[] tagss) {
    Assert.notNull(c, "没有参数");
    if (tagss != null) {
      Case save = caseService.save(c);
      for (String tags : tagss) {
        if (tags.isEmpty() || "".equals(tags)) {
          continue;
        }
        Tag byName = tagService.findByName(tags);
        if (byName == null) {
          byName = new Tag();
          byName.setName(tags);
          byName.setIsExist(true);
          byName = tagService.save(byName);
          List<Tag> all = tagService.findAll();
          InitListener.getApplicatonContext().getServletContext().setAttribute("tags", all);
        }
        List<Case> cases = byName.getCases();
        cases.add(save);
        byName.setCases(cases);
        tagService.save(byName);
        List<Tag> tags1 = c.getTags();
        tags1.add(byName);
        c.setTags(tags1);
      }
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH");

    c.setIsExist(true);
    c.setDate(new Date());
    c.setTimes(0L);
    c.setLibId(simpleDateFormat.format(new Date()));
    System.out.println(c);

    Type type = c.getType();
    List<Case> cases = type.getCases();
    cases.add(c);
    type.setCases(cases);

    c.setType(type);
    caseService.save(c);
    typeService.save(type);
    model.addAttribute("msg", "添加成功");
    return "admin/case_add";
  }

  /**
   * 修改案例
   *
   * @param c
   * @return
   */
  @Transactional(rollbackFor = Exception.class)
  @PostMapping(value = "/change.html")
  public String changeCase(Case c, String[] tagss, Model model) {
    Assert.notNull(c, "没有参数");
    List<Tag> tagslist = new ArrayList<>();

    if (tagss != null) {
      for (String tags : tagss) {
        if (tags.isEmpty() || "".equals(tags)) {
          continue;
        }
        Tag byName = tagService.findByName(tags);
        if (byName == null) {
          byName = new Tag();
          byName.setName(tags);
          byName.setIsExist(true);
          byName = tagService.save(byName);
          List<Tag> all = tagService.findAll();
          InitListener.getApplicatonContext().getServletContext().setAttribute("tags", all);
        }
        List<Case> cases = byName.getCases();
        cases.add(c);
        byName.setCases(cases);
        tagService.save(byName);
        tagslist.add(byName);
        c.setTags(tagslist);
      }
    }

    Case byId = caseService.findById(c.getId());
    Type type1 = byId.getType();
    List<Case> cases1 = type1.getCases();
    cases1.remove(byId);
    type1.setCases(cases1);
    typeService.save(type1);
    if (!c.getName().equals(byId.getName())) {
      List<Doc> contents = byId.getContents();
      for (Doc doc : contents) {
        String path = doc.getPath();
        String replace = path.replace(byId.getName(), c.getName());
        doc.setPath(replace);
        File file = new File(basePath + path);
        File file1 = new File(basePath + replace);
        creatDir(file1.getParentFile());
        file.renameTo(file1);
      }
      byId.setContents(contents);

      List<Doc> solves = byId.getSolves();
      for (Doc doc : solves) {
        String path = doc.getPath();
        String replace = path.replace(byId.getName(), c.getName());
        doc.setPath(replace);
        File file = new File(basePath + path);
        File file1 = new File(basePath + replace);
        creatDir(file1.getParentFile());
        file.renameTo(file1);
      }
      byId.setSolves(solves);
      docService.saveAll(contents);
      docService.saveAll(solves);
    }

    if (!c.getType().getId().equals(byId.getType().getId())) {
      List<Doc> contents = byId.getContents();
      for (Doc doc : contents) {
        String path = doc.getPath();
        String replace = path.replace(byId.getType().getName(), c.getType().getName());
        doc.setPath(replace);
        File file = new File(basePath + path);
        File file1 = new File(basePath + replace);
        creatDir(file1.getParentFile());
        file.renameTo(file1);
      }
      byId.setContents(contents);

      List<Doc> solves = byId.getSolves();
      for (Doc doc : solves) {
        String path = doc.getPath();
        String replace = path.replace(byId.getType().getName(), c.getType().getName());
        doc.setPath(replace);
        File file = new File(basePath + path);
        File file1 = new File(basePath + replace);
        creatDir(file1.getParentFile());
        file.renameTo(file1);
      }
      byId.setSolves(solves);
      docService.saveAll(contents);
      docService.saveAll(solves);
    }

    c.setContents(byId.getContents());
    c.setSolves(byId.getSolves());
    c.setTimes(byId.getTimes());
    c.setLibId(byId.getLibId());
    c.setDate(byId.getDate());
    caseService.save(c);
    Type type = c.getType();
    List<Case> cases = type.getCases();
    cases.add(c);
    type.setCases(cases);
    typeService.save(type);
    model.addAttribute("msg", "变更成功");
    return "admin/case_change";
  }

  private void creatDir(File file) {
    while (!file.exists()) {
      creatDir(file.getParentFile());
      file.mkdir();
    }
  }

  @RolesAllowed("ROLE_ADMIN")
  @GetMapping(value = "/get")
  public String getAll(Model model) {
    List<Case> allByIsExist = caseService.findAllByIsExist(true);
    model.addAttribute("cases", allByIsExist);
    return "admin/case_all";
  }

  @GetMapping(value = "/get/{id}/case_info.html")
  public String findCaseById(@PathVariable(name = "id") Case c, Model model) {
    model.addAttribute("case", c);
    return "admin/case_change";
  }
}
