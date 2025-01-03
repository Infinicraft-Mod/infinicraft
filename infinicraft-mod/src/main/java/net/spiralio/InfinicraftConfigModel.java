package net.spiralio;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.SectionHeader;

@Modmenu(modId = "infinicraft")
@Config(name = "infinicraft-config", wrapperName = "InfinicraftConfig")
public class InfinicraftConfigModel {

  @SectionHeader("mainSection")
  public boolean SHOW_RECIPE = true;

  public boolean SHOW_DESCRIPTION = true;

  public String INFINICRAFT_SERVER = "http://localhost:17707";
  public int SECONDS_TO_TIMEOUT = 60;
}
